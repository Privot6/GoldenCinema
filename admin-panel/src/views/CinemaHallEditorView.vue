<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { AlertCircle, Save, Trash2 } from 'lucide-vue-next'

const GRID_SIZE = 30

interface SeatGridItemDto {
  gridRow: number
  gridCol: number
  rowLabel: string
  seatNumber: number
}

const route = useRoute()
const router = useRouter()

const hallId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => hallId.value !== null)

const hallName = ref('')
const error = ref('')
const saving = ref(false)
const loading = ref(false)

// 2D boolean grid: grid[row][col] = active
const grid = ref<boolean[][]>(
  Array.from({ length: GRID_SIZE }, () => Array(GRID_SIZE).fill(false))
)

// drag support
const isDragging = ref(false)
const dragValue = ref(true)

function getCell(r: number, c: number): boolean {
  return grid.value[r]?.[c] ?? false
}

function setCell(r: number, c: number, val: boolean): void {
  const row = grid.value[r]
  if (row !== undefined) row[c] = val
}

function startDrag(row: number, col: number) {
  isDragging.value = true
  dragValue.value = !getCell(row, col)
  setCell(row, col, dragValue.value)
}

function dragOver(row: number, col: number) {
  if (isDragging.value) {
    setCell(row, col, dragValue.value)
  }
}

function stopDrag() {
  isDragging.value = false
}

// Compute rowLabel for each gridRow that has at least one active cell.
// Active rows are sorted by gridRow index and labeled A, B, C...
const rowLabelMap = computed<Map<number, string>>(() => {
  const activeRows: number[] = []
  for (let r = 0; r < GRID_SIZE; r++) {
    if (grid.value[r]?.some(Boolean)) activeRows.push(r)
  }
  const map = new Map<number, string>()
  activeRows.forEach((r, idx) => {
    map.set(r, String.fromCharCode(65 + idx))
  })
  return map
})

// Compute seat number for each active cell: per row, count active cells left to right.
function getSeatNumber(row: number, col: number): number {
  let count = 0
  for (let c = 0; c <= col; c++) {
    if (getCell(row, c)) count++
  }
  return count
}

// Build the seats array to POST/PUT
function buildSeats(): SeatGridItemDto[] {
  const seats: SeatGridItemDto[] = []
  for (let r = 0; r < GRID_SIZE; r++) {
    const label = rowLabelMap.value.get(r)
    if (!label) continue
    for (let c = 0; c < GRID_SIZE; c++) {
      if (getCell(r, c)) {
        seats.push({ gridRow: r, gridCol: c, rowLabel: label, seatNumber: getSeatNumber(r, c) })
      }
    }
  }
  return seats
}

function getCellLabel(row: number, col: number): string {
  if (!getCell(row, col)) return ''
  const label = rowLabelMap.value.get(row) ?? ''
  return `${label}${getSeatNumber(row, col)}`
}

async function save() {
  if (!hallName.value.trim()) { error.value = 'Podaj nazwę sali.'; return }
  const seats = buildSeats()
  if (seats.length === 0) { error.value = 'Zaznacz co najmniej jedno miejsce.'; return }
  error.value = ''
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put(`/halls/${hallId.value}`, { name: hallName.value, seats })
    } else {
      await api.post('/halls', { name: hallName.value, seats })
    }
    router.push('/halls')
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Błąd zapisu sali.'
  } finally {
    saving.value = false
  }
}

function clearGrid() {
  grid.value = Array.from({ length: GRID_SIZE }, () => Array(GRID_SIZE).fill(false))
}

onMounted(async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const { data } = await api.get(`/halls/${hallId.value}`)
    hallName.value = data.name
    const newGrid: boolean[][] = Array.from({ length: GRID_SIZE }, () => Array(GRID_SIZE).fill(false))
    for (const seat of data.seats as SeatGridItemDto[]) {
      if (seat.gridRow < GRID_SIZE && seat.gridCol < GRID_SIZE) {
        const row = newGrid[seat.gridRow]
        if (row !== undefined) row[seat.gridCol] = true
      }
    }
    grid.value = newGrid
  } catch {
    error.value = 'Nie udało się załadować sali.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="p-6 space-y-6" @mouseup="stopDrag" @mouseleave="stopDrag">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">{{ isEdit ? 'Edytuj salę' : 'Nowa sala kinowa' }}</h1>
      <div class="flex gap-2">
        <Button variant="outline" size="sm" @click="clearGrid">
          <Trash2 class="w-4 h-4 mr-1" /> Wyczyść
        </Button>
        <Button size="sm" @click="save" :disabled="saving">
          <Save class="w-4 h-4 mr-1" /> {{ saving ? 'Zapisywanie...' : 'Zapisz' }}
        </Button>
      </div>
    </div>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <Card>
      <CardContent class="pt-4 pb-2">
        <div class="flex items-center gap-4 max-w-sm">
          <Label for="hall-name">Nazwa sali</Label>
          <Input id="hall-name" v-model="hallName" placeholder="np. Sala Główna" />
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardHeader class="pb-2">
        <CardTitle class="text-base">Układ sali (siatka 30×30)</CardTitle>
        <p class="text-sm text-muted-foreground">Kliknij lub przeciągnij, aby zaznaczyć miejsca. Litery rzędów i numery są obliczane automatycznie.</p>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-10 text-muted-foreground">Ładowanie...</div>
        <div v-else class="overflow-auto">
          <!-- Row labels on left -->
          <div class="flex gap-1 items-start">
            <!-- Left label column -->
            <div class="flex flex-col gap-[2px] mr-1">
              <div class="w-5 h-5" /> <!-- spacer header -->
              <div
                v-for="r in GRID_SIZE"
                :key="r"
                class="w-5 h-5 flex items-center justify-center text-[9px] font-semibold text-muted-foreground"
              >
                {{ rowLabelMap.get(r - 1) ?? '' }}
              </div>
            </div>

            <!-- Grid -->
            <div class="flex flex-col gap-[2px]">
              <!-- Column numbers header -->
              <div class="flex gap-[2px]">
                <div
                  v-for="c in GRID_SIZE"
                  :key="c"
                  class="w-5 h-5 flex items-center justify-center text-[8px] text-muted-foreground"
                >
                  {{ c }}
                </div>
              </div>

              <!-- Grid rows -->
              <div
                v-for="r in GRID_SIZE"
                :key="r"
                class="flex gap-[2px]"
              >
                <div
                  v-for="c in GRID_SIZE"
                  :key="c"
                  class="w-5 h-5 rounded-[2px] flex items-center justify-center cursor-pointer select-none text-[7px] font-medium transition-colors"
                  :class="getCell(r-1, c-1)
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted hover:bg-muted/70'"
                  @mousedown="startDrag(r-1, c-1)"
                  @mouseover="dragOver(r-1, c-1)"
                >
                  {{ getCellLabel(r - 1, c - 1) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Legend -->
          <div class="flex gap-4 mt-4 text-xs text-muted-foreground">
            <span class="flex items-center gap-1">
              <span class="inline-block w-3 h-3 rounded-[2px] bg-primary" /> Miejsce aktywne
            </span>
            <span class="flex items-center gap-1">
              <span class="inline-block w-3 h-3 rounded-[2px] bg-muted" /> Puste (korytarz/schody)
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
