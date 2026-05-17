<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle, Plus, Pencil, ChevronDown, ChevronUp, Trash2 } from 'lucide-vue-next'

const GRID_SIZE = 30

interface HallDto {
  id: number
  name: string
}

interface SeatGridItemDto {
  gridRow: number
  gridCol: number
  rowLabel: string
  seatNumber: number
}

interface HallLayout {
  id: number
  name: string
  seats: SeatGridItemDto[]
}

const router = useRouter()
const halls = ref<HallDto[]>([])
const error = ref('')
const loading = ref(false)

// per-hall expanded state and loaded layout
const expanded = ref<Record<number, boolean>>({})
const layouts = ref<Record<number, HallLayout | null>>({})
const layoutLoading = ref<Record<number, boolean>>({})

async function fetchHalls() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<HallDto[]>('/halls')
    halls.value = data
    page.value = 1
  } catch {
    error.value = 'Nie udało się pobrać listy sal.'
  } finally {
    loading.value = false
  }
}

const PAGE_SIZE = 25
const page = ref(1)
const paged = computed(() => halls.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
const totalPages = computed(() => Math.max(1, Math.ceil(halls.value.length / PAGE_SIZE)))

async function deleteHall(hall: HallDto) {
  if (!window.confirm(`Czy na pewno usunąć salę „${hall.name}"? Ta operacja jest nieodwracalna.`)) return
  try {
    await api.delete(`/halls/${hall.id}`)
    halls.value = halls.value.filter(h => h.id !== hall.id)
  } catch {
    error.value = 'Nie udało się usunąć sali.'
  }
}

async function toggleLayout(hall: HallDto) {
  if (expanded.value[hall.id]) {
    expanded.value[hall.id] = false
    return
  }
  expanded.value[hall.id] = true
  if (layouts.value[hall.id] !== undefined) return // already loaded

  layoutLoading.value[hall.id] = true
  try {
    const { data } = await api.get<HallLayout>(`/halls/${hall.id}`)
    layouts.value[hall.id] = data
  } catch {
    layouts.value[hall.id] = null
  } finally {
    layoutLoading.value[hall.id] = false
  }
}

// Build a 30x30 boolean matrix from a loaded layout
function buildGrid(layout: HallLayout): boolean[][] {
  const g: boolean[][] = Array.from({ length: GRID_SIZE }, () => Array(GRID_SIZE).fill(false))
  for (const seat of layout.seats) {
    if (seat.gridRow < GRID_SIZE && seat.gridCol < GRID_SIZE) {
      const row = g[seat.gridRow]
      if (row !== undefined) row[seat.gridCol] = true
    }
  }
  return g
}

// Row label map: active gridRow → letter
function buildRowLabels(layout: HallLayout): Map<number, string> {
  const activeRows = [...new Set(layout.seats.map(s => s.gridRow))].sort((a, b) => a - b)
  const map = new Map<number, string>()
  activeRows.forEach((r, i) => map.set(r, String.fromCharCode(65 + i)))
  return map
}

onMounted(fetchHalls)
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Sale kinowe</h1>
      <Button size="sm" @click="router.push('/halls/new')">
        <Plus class="w-4 h-4 mr-1" /> Nowa sala
      </Button>
    </div>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <div v-if="loading" class="text-center py-16 text-muted-foreground">Ładowanie...</div>

    <div v-else-if="halls.length === 0" class="text-center py-16 text-muted-foreground">
      Brak sal. Dodaj pierwszą salę klikając „Nowa sala".
    </div>

    <div v-else class="space-y-4">
      <Card v-for="hall in paged" :key="hall.id">
        <CardHeader class="pb-3">
          <div class="flex items-center justify-between">
            <CardTitle class="text-base">{{ hall.name }}</CardTitle>
            <div class="flex gap-2">
              <Button variant="outline" size="sm" @click="toggleLayout(hall)">
                <ChevronUp v-if="expanded[hall.id]" class="w-4 h-4 mr-1" />
                <ChevronDown v-else class="w-4 h-4 mr-1" />
                {{ expanded[hall.id] ? 'Ukryj układ' : 'Podgląd układu' }}
              </Button>
              <Button size="sm" @click="router.push(`/halls/${hall.id}`)">
                <Pencil class="w-4 h-4 mr-1" /> Edytuj
              </Button>
              <Button variant="destructive" size="sm" @click="deleteHall(hall)">
                <Trash2 class="w-4 h-4 mr-1" /> Usuń
              </Button>
            </div>
          </div>
        </CardHeader>

        <CardContent v-if="expanded[hall.id]">
          <div v-if="layoutLoading[hall.id]" class="text-sm text-muted-foreground py-4 text-center">
            Ładowanie układu...
          </div>
          <div v-else-if="!layouts[hall.id]" class="text-sm text-destructive py-2">
            Nie udało się załadować układu.
          </div>
          <template v-else>
            <p class="text-xs text-muted-foreground mb-3">
              Łącznie miejsc: <strong>{{ layouts[hall.id]!.seats.length }}</strong>
            </p>

            <!-- Mini grid preview -->
            <div class="overflow-auto">
              <div class="flex gap-1 items-start">
                <!-- Row labels -->
                <div class="flex flex-col gap-[1px] mr-1">
                  <div class="w-4 h-4" />
                  <div
                    v-for="r in GRID_SIZE"
                    :key="r"
                    class="w-4 h-4 flex items-center justify-center text-[8px] font-semibold text-muted-foreground"
                  >
                    {{ buildRowLabels(layouts[hall.id]!).get(r - 1) ?? '' }}
                  </div>
                </div>

                <!-- Grid cells -->
                <div class="flex flex-col gap-[1px]">
                  <div class="flex gap-[1px]">
                    <div
                      v-for="c in GRID_SIZE"
                      :key="c"
                      class="w-4 h-4 flex items-center justify-center text-[7px] text-muted-foreground"
                    >
                      {{ c % 5 === 0 ? c : '' }}
                    </div>
                  </div>
                  <div
                    v-for="r in GRID_SIZE"
                    :key="r"
                    class="flex gap-[1px]"
                  >
                    <div
                      v-for="c in GRID_SIZE"
                      :key="c"
                      class="w-4 h-4 rounded-[2px]"
                      :class="buildGrid(layouts[hall.id]!)[r-1]?.[c-1]
                        ? 'bg-primary'
                        : 'bg-muted'"
                    />
                  </div>
                </div>
              </div>

              <!-- Legend -->
              <div class="flex gap-4 mt-3 text-xs text-muted-foreground">
                <span class="flex items-center gap-1">
                  <span class="inline-block w-3 h-3 rounded-[2px] bg-primary" /> Miejsce
                </span>
                <span class="flex items-center gap-1">
                  <span class="inline-block w-3 h-3 rounded-[2px] bg-muted" /> Puste
                </span>
              </div>
            </div>
          </template>
        </CardContent>
      </Card>
    </div>

    <div v-if="totalPages > 1" class="flex items-center justify-between">
      <span class="text-sm text-muted-foreground">Strona {{ page }} z {{ totalPages }} ({{ halls.length }} sal)</span>
      <div class="flex gap-2">
        <Button variant="outline" size="sm" :disabled="page === 1" @click="page--">Poprzednia</Button>
        <Button variant="outline" size="sm" :disabled="page === totalPages" @click="page++">Następna</Button>
      </div>
    </div>
  </div>
</template>
