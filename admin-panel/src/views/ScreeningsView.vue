<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import api from '@/api/axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import AppModal from '@/components/AppModal.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle, ChevronDown, ChevronRight, Film, Pencil, Plus, Trash2 } from 'lucide-vue-next'

interface HallOption { id: number; name: string }

interface Screening {
  id: number
  startTime: string
  endTime: string
  basePrice: number
  status: 'ZAPLANOWANY' | 'ANULOWANY' | 'ZAKONCZONY'
  movie: { id: number; title: string }
  hall: HallOption
}

interface MovieWithScreenings {
  id: number
  title: string
  screenings: Screening[]
  loaded: boolean
  loading: boolean
  error: string
  page: number
}

const MOVIE_PAGE_SIZE = 10

const movies = ref<MovieWithScreenings[]>([])
const halls = ref<HallOption[]>([])
const loading = ref(false)
const error = ref('')
const showForm = ref(false)
const editingId = ref<number | null>(null)
const expandedMovieIds = ref<Set<number>>(new Set())

const form = reactive({
  movieId: '' as string | number,
  hallId: '' as string | number,
  date: '',
  startHour: '',
  durationMinutes: '' as string | number,
  basePrice: '' as string | number
})

const endTimeDisplay = computed((): string => {
  if (!form.date || !form.startHour || !form.durationMinutes || Number(form.durationMinutes) <= 0) return ''
  const start = new Date(`${form.date}T${form.startHour}`)
  if (isNaN(start.getTime())) return ''
  const end = new Date(start.getTime() + Number(form.durationMinutes) * 60000)
  return new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(end)
})

function buildEndIso(): string {
  const start = new Date(`${form.date}T${form.startHour}`)
  const end = new Date(start.getTime() + Number(form.durationMinutes) * 60000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${end.getFullYear()}-${pad(end.getMonth() + 1)}-${pad(end.getDate())}T${pad(end.getHours())}:${pad(end.getMinutes())}`
}

function resetForm() {
  form.movieId = ''; form.hallId = ''; form.date = ''
  form.startHour = ''; form.durationMinutes = ''; form.basePrice = ''
  editingId.value = null
}

function openCreate(movieId?: number) {
  resetForm()
  if (movieId !== undefined) form.movieId = movieId
  showForm.value = true
}

function openEdit(s: Screening) {
  resetForm()
  form.movieId = s.movie.id
  form.hallId = s.hall.id
  form.date = s.startTime.slice(0, 10)
  form.startHour = s.startTime.slice(11, 16)
  const diffMs = new Date(s.endTime).getTime() - new Date(s.startTime).getTime()
  form.durationMinutes = Math.round(diffMs / 60000)
  form.basePrice = s.basePrice
  editingId.value = s.id
  showForm.value = true
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

function moviePagedScreenings(movie: MovieWithScreenings): Screening[] {
  return movie.screenings.slice((movie.page - 1) * MOVIE_PAGE_SIZE, movie.page * MOVIE_PAGE_SIZE)
}

function movieTotalPages(movie: MovieWithScreenings): number {
  return Math.max(1, Math.ceil(movie.screenings.length / MOVIE_PAGE_SIZE))
}

function formMovieTitle(): string {
  const id = Number(form.movieId)
  return movies.value.find(m => m.id === id)?.title ?? ''
}

async function fetchMovieList() {
  loading.value = true
  error.value = ''
  try {
    const [moviesRes, hallsRes] = await Promise.all([
      api.get<{ id: number; title: string }[]>('/movies'),
      api.get<HallOption[]>('/halls')
    ])
    movies.value = moviesRes.data.map(m => ({
      id: m.id,
      title: m.title,
      screenings: [],
      loaded: false,
      loading: false,
      error: '',
      page: 1
    }))
    halls.value = hallsRes.data
  } catch {
    error.value = 'Nie udało się pobrać listy filmów.'
  } finally {
    loading.value = false
  }
}

async function fetchMovieScreenings(movieId: number) {
  const movie = movies.value.find(m => m.id === movieId)
  if (!movie || movie.loading) return
  movie.loading = true
  movie.error = ''
  try {
    const { data } = await api.get<Screening[]>(`/movies/${movieId}/screenings`)
    movie.screenings = data
    movie.loaded = true
    movie.page = 1
  } catch {
    movie.error = 'Nie udało się pobrać seansów dla tego filmu.'
  } finally {
    movie.loading = false
  }
}

function toggleMovie(movieId: number) {
  if (expandedMovieIds.value.has(movieId)) {
    expandedMovieIds.value.delete(movieId)
  } else {
    expandedMovieIds.value.add(movieId)
    const movie = movies.value.find(m => m.id === movieId)
    if (movie && !movie.loaded) fetchMovieScreenings(movieId)
  }
  expandedMovieIds.value = new Set(expandedMovieIds.value)
}

async function saveScreening() {
  error.value = ''
  const payload = {
    movieId: Number(form.movieId),
    hallId: Number(form.hallId),
    startTime: `${form.date}T${form.startHour}`,
    endTime: buildEndIso(),
    basePrice: Number(form.basePrice)
  }
  try {
    if (editingId.value !== null) {
      const { data: updated } = await api.put<Screening>(`/admin/screenings/${editingId.value}`, payload)
      for (const movie of movies.value) {
        const idx = movie.screenings.findIndex(s => s.id === editingId.value)
        if (idx !== -1) { movie.screenings[idx] = updated; break }
      }
    } else {
      const { data: newScreening } = await api.post<Screening>('/admin/screenings', payload)
      const targetMovie = movies.value.find(m => m.id === newScreening.movie.id)
      if (targetMovie && targetMovie.loaded) {
        targetMovie.screenings.push(newScreening)
        targetMovie.page = 1
      }
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
    for (const movie of movies.value) {
      const row = movie.screenings.find(x => x.id === s.id)
      if (row) { row.status = 'ANULOWANY'; break }
    }
  } catch {
    error.value = 'Nie udało się anulować seansu.'
  }
}

onMounted(fetchMovieList)
</script>

<template>
  <div class="p-6 space-y-6">

    <!-- Header -->
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Seanse</h1>
      <Button size="sm" @click="openCreate()">
        <Plus class="w-4 h-4 mr-1" /> Nowy seans
      </Button>
    </div>

    <!-- Top-level error -->
    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <!-- Form modal -->
    <AppModal
      :open="showForm"
      :title="editingId ? 'Edytuj seans' : form.movieId ? `Nowy seans — ${formMovieTitle()}` : 'Nowy seans'"
      @close="closeForm"
    >
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
          <Label>Data seansu *</Label>
          <Input v-model="form.date" type="date" />
        </div>
        <div class="space-y-1">
          <Label>Godzina rozpoczęcia *</Label>
          <Input v-model="form.startHour" type="time" />
        </div>
        <div class="space-y-1">
          <Label>Czas trwania (min) *</Label>
          <Input v-model="form.durationMinutes" type="number" min="1" step="1" placeholder="120" />
        </div>
        <div class="space-y-1">
          <Label>Cena bazowa (PLN) *</Label>
          <Input v-model="form.basePrice" type="number" min="0" step="0.01" placeholder="25.00" />
        </div>
      </div>
      <div v-if="endTimeDisplay" class="mt-3 flex items-center gap-2 text-sm text-muted-foreground">
        <span class="font-medium text-foreground">Zakończenie:</span>
        {{ endTimeDisplay }}
      </div>
      <div class="flex gap-2 mt-6">
        <Button @click="saveScreening">{{ editingId ? 'Zapisz zmiany' : 'Dodaj' }}</Button>
        <Button variant="outline" @click="closeForm">Anuluj</Button>
      </div>
    </AppModal>

    <!-- Loading skeleton for movies list -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="h-16 rounded-lg bg-secondary/50 animate-pulse" />
    </div>

    <!-- Empty state -->
    <div v-else-if="movies.length === 0"
         class="py-12 text-center text-sm text-muted-foreground">
      Brak filmów w systemie.
    </div>

    <!-- Movie accordion list -->
    <div v-else class="space-y-3">
      <Card v-for="movie in movies" :key="movie.id" class="overflow-hidden">

        <!-- Accordion header -->
        <button
          type="button"
          class="w-full flex items-center justify-between px-4 py-3 text-left hover:bg-secondary/30 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          @click="toggleMovie(movie.id)"
        >
          <div class="flex items-center gap-3 min-w-0">
            <Film class="w-5 h-5 text-muted-foreground shrink-0" />
            <span class="font-medium text-foreground truncate">{{ movie.title }}</span>
            <Badge v-if="movie.loaded" variant="outline" class="shrink-0">
              {{ movie.screenings.length }}
            </Badge>
          </div>
          <div class="flex items-center gap-2 shrink-0 ml-4">
            <Button
              size="sm"
              variant="ghost"
              class="gap-1 text-muted-foreground hover:text-foreground"
              @click.stop="openCreate(movie.id)"
            >
              <Plus class="w-4 h-4" />
              Dodaj seans
            </Button>
            <ChevronDown v-if="expandedMovieIds.has(movie.id)" class="w-4 h-4 text-muted-foreground" />
            <ChevronRight v-else class="w-4 h-4 text-muted-foreground" />
          </div>
        </button>

        <!-- Accordion body -->
        <div v-if="expandedMovieIds.has(movie.id)">
          <div class="border-t" />

          <!-- Per-movie loading -->
          <div v-if="movie.loading" class="space-y-2 p-4">
            <div v-for="i in 3" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
          </div>

          <!-- Per-movie error -->
          <Alert v-else-if="movie.error" variant="destructive" class="m-4">
            <AlertCircle class="w-4 h-4" />
            <AlertDescription>{{ movie.error }}</AlertDescription>
          </Alert>

          <!-- Per-movie empty -->
          <div v-else-if="movie.loaded && movie.screenings.length === 0"
               class="px-4 py-8 text-center text-sm text-muted-foreground">
            Brak seansów dla tego filmu.
            <button
              type="button"
              class="ml-1 underline underline-offset-2 hover:text-foreground transition-colors"
              @click="openCreate(movie.id)"
            >
              Dodaj pierwszy seans
            </button>
          </div>

          <!-- Screenings table -->
          <template v-else-if="movie.loaded">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sala</TableHead>
                  <TableHead>Początek</TableHead>
                  <TableHead>Koniec</TableHead>
                  <TableHead>Cena</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead class="text-right">Akcje</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableEmpty v-if="moviePagedScreenings(movie).length === 0">Brak seansów</TableEmpty>
                <TableRow v-for="s in moviePagedScreenings(movie)" :key="s.id">
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
                      <Button variant="ghost" size="sm"
                        class="text-destructive hover:text-destructive"
                        @click="cancelScreening(s)"
                        :disabled="s.status !== 'ZAPLANOWANY'">
                        <Trash2 class="w-4 h-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>

            <!-- Pagination footer -->
            <div v-if="movieTotalPages(movie) > 1"
                 class="flex items-center justify-between px-4 py-3 border-t">
              <span class="text-sm text-muted-foreground">
                Strona {{ movie.page }} z {{ movieTotalPages(movie) }}
                ({{ movie.screenings.length }} seansów)
              </span>
              <div class="flex gap-2">
                <Button variant="outline" size="sm"
                  :disabled="movie.page === 1"
                  @click="movie.page--">
                  Poprzednia
                </Button>
                <Button variant="outline" size="sm"
                  :disabled="movie.page === movieTotalPages(movie)"
                  @click="movie.page++">
                  Następna
                </Button>
              </div>
            </div>
          </template>

        </div>
      </Card>
    </div>

  </div>
</template>
