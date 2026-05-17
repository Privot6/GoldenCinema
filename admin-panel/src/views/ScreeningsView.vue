<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import api from '@/api/axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle, Plus, Pencil, Trash2, X } from 'lucide-vue-next'

interface MovieOption { id: number; title: string }
interface HallOption  { id: number; name: string }

interface Screening {
  id: number
  startTime: string
  endTime: string
  basePrice: number
  status: 'ZAPLANOWANY' | 'ANULOWANY' | 'ZAKONCZONY'
  movie: MovieOption
  hall: HallOption
}

const screenings = ref<Screening[]>([])
const movies = ref<MovieOption[]>([])
const halls = ref<HallOption[]>([])
const loading = ref(false)
const error = ref('')
const showForm = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  movieId: '' as string | number,
  hallId: '' as string | number,
  startTime: '',
  endTime: '',
  basePrice: '' as string | number
})

function resetForm() {
  form.movieId = ''; form.hallId = ''; form.startTime = ''
  form.endTime = ''; form.basePrice = ''; editingId.value = null
}

function toLocalInput(iso: string) {
  return iso ? iso.slice(0, 16) : ''
}

function openCreate() { resetForm(); showForm.value = true }

function openEdit(s: Screening) {
  form.movieId = s.movie.id; form.hallId = s.hall.id
  form.startTime = toLocalInput(s.startTime)
  form.endTime = toLocalInput(s.endTime)
  form.basePrice = s.basePrice; editingId.value = s.id; showForm.value = true
}

function closeForm() { showForm.value = false; resetForm() }

function statusVariant(status: Screening['status']) {
  if (status === 'ZAPLANOWANY') return 'default'
  if (status === 'ZAKONCZONY') return 'secondary'
  return 'destructive'
}

function statusLabel(status: Screening['status']) {
  if (status === 'ZAPLANOWANY') return 'Zaplanowany'
  if (status === 'ZAKONCZONY') return 'Zakończony'
  return 'Anulowany'
}

function formatDate(iso: string) {
  return new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(new Date(iso))
}

async function fetchData() {
  loading.value = true; error.value = ''
  try {
    const [s, m, h] = await Promise.all([
      api.get<Screening[]>('/admin/screenings'),
      api.get<MovieOption[]>('/movies'),
      api.get<HallOption[]>('/halls')
    ])
    screenings.value = s.data; movies.value = m.data; halls.value = h.data
  } catch { error.value = 'Nie udało się pobrać danych.' }
  finally { loading.value = false }
}

async function saveScreening() {
  error.value = ''
  const payload = {
    movieId: Number(form.movieId),
    hallId: Number(form.hallId),
    startTime: form.startTime,
    endTime: form.endTime,
    basePrice: Number(form.basePrice)
  }
  try {
    if (editingId.value !== null) {
      const { data } = await api.put<Screening>(`/admin/screenings/${editingId.value}`, payload)
      const idx = screenings.value.findIndex(s => s.id === editingId.value)
      if (idx !== -1) screenings.value[idx] = data
    } else {
      const { data } = await api.post<Screening>('/admin/screenings', payload)
      screenings.value.unshift(data)
    }
    closeForm()
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Błąd zapisu.'
  }
}

async function cancelScreening(s: Screening) {
  if (!window.confirm(`Anulować seans „${s.movie.title}" (${formatDate(s.startTime)})?`)) return
  try {
    await api.delete(`/admin/screenings/${s.id}`)
    const idx = screenings.value.findIndex(x => x.id === s.id)
    const row = screenings.value[idx]
    if (idx !== -1 && row !== undefined) row.status = 'ANULOWANY'
  } catch { error.value = 'Nie udało się anulować seansu.' }
}

onMounted(fetchData)
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Seanse</h1>
      <Button size="sm" @click="openCreate">
        <Plus class="w-4 h-4 mr-1" /> Nowy seans
      </Button>
    </div>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <!-- Form -->
    <Card v-if="showForm">
      <CardHeader class="pb-3">
        <div class="flex items-center justify-between">
          <CardTitle class="text-base">{{ editingId ? 'Edytuj seans' : 'Nowy seans' }}</CardTitle>
          <Button variant="ghost" size="sm" @click="closeForm"><X class="w-4 h-4" /></Button>
        </div>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label>Film *</Label>
            <select v-model="form.movieId"
              class="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm">
              <option value="">-- wybierz film --</option>
              <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.title }}</option>
            </select>
          </div>
          <div class="space-y-1">
            <Label>Sala *</Label>
            <select v-model="form.hallId"
              class="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm">
              <option value="">-- wybierz salę --</option>
              <option v-for="h in halls" :key="h.id" :value="h.id">{{ h.name }}</option>
            </select>
          </div>
          <div class="space-y-1">
            <Label>Początek *</Label>
            <Input v-model="form.startTime" type="datetime-local" />
          </div>
          <div class="space-y-1">
            <Label>Koniec *</Label>
            <Input v-model="form.endTime" type="datetime-local" />
          </div>
          <div class="space-y-1">
            <Label>Cena bazowa (PLN) *</Label>
            <Input v-model="form.basePrice" type="number" min="0" step="0.01" placeholder="25.00" />
          </div>
        </div>
        <div class="flex gap-2 mt-4">
          <Button @click="saveScreening">{{ editingId ? 'Zapisz zmiany' : 'Dodaj' }}</Button>
          <Button variant="outline" @click="closeForm">Anuluj</Button>
        </div>
      </CardContent>
    </Card>

    <!-- Table -->
    <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="space-y-3 p-4">
          <div v-for="i in 6" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>Film</TableHead>
              <TableHead>Sala</TableHead>
              <TableHead>Początek</TableHead>
              <TableHead>Koniec</TableHead>
              <TableHead>Cena</TableHead>
              <TableHead>Status</TableHead>
              <TableHead class="text-right">Akcje</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="screenings.length === 0">Brak seansów</TableEmpty>
            <TableRow v-for="s in screenings" :key="s.id">
              <TableCell class="font-medium">{{ s.movie.title }}</TableCell>
              <TableCell class="text-muted-foreground">{{ s.hall.name }}</TableCell>
              <TableCell class="tabular-nums text-muted-foreground">{{ formatDate(s.startTime) }}</TableCell>
              <TableCell class="tabular-nums text-muted-foreground">{{ formatDate(s.endTime) }}</TableCell>
              <TableCell class="tabular-nums">{{ s.basePrice.toFixed(2) }} zł</TableCell>
              <TableCell>
                <Badge :variant="statusVariant(s.status)">{{ statusLabel(s.status) }}</Badge>
              </TableCell>
              <TableCell class="text-right">
                <div class="flex gap-1 justify-end">
                  <Button variant="ghost" size="sm" @click="openEdit(s)"
                    :disabled="s.status !== 'ZAPLANOWANY'">
                    <Pencil class="w-4 h-4" />
                  </Button>
                  <Button variant="ghost" size="sm" class="text-destructive hover:text-destructive"
                    @click="cancelScreening(s)" :disabled="s.status !== 'ZAPLANOWANY'">
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
