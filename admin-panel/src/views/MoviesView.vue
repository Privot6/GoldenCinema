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

interface Movie {
  id: number
  title: string
  description: string
  durationMinutes: number
  ageRating: string
  language: string
  subtitles: string
  genre: string
  posterUrl: string
  isActive: boolean
}

const movies = ref<Movie[]>([])
const loading = ref(false)
const error = ref('')
const showForm = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  title: '', description: '', durationMinutes: '' as string | number,
  ageRating: '', language: '', subtitles: '', genre: '', posterUrl: ''
})

function resetForm() {
  form.title = ''; form.description = ''; form.durationMinutes = ''
  form.ageRating = ''; form.language = ''; form.subtitles = ''
  form.genre = ''; form.posterUrl = ''
  editingId.value = null
}

function openCreate() { resetForm(); showForm.value = true }

function openEdit(m: Movie) {
  form.title = m.title; form.description = m.description
  form.durationMinutes = m.durationMinutes; form.ageRating = m.ageRating
  form.language = m.language; form.subtitles = m.subtitles
  form.genre = m.genre; form.posterUrl = m.posterUrl
  editingId.value = m.id; showForm.value = true
}

function closeForm() { showForm.value = false; resetForm() }

async function fetchMovies() {
  loading.value = true; error.value = ''
  try {
    const { data } = await api.get<Movie[]>('/movies')
    movies.value = data
  } catch { error.value = 'Nie udało się pobrać filmów.' }
  finally { loading.value = false }
}

async function saveMovie() {
  error.value = ''
  const payload = { ...form, durationMinutes: Number(form.durationMinutes) }
  try {
    if (editingId.value !== null) {
      const { data } = await api.put<Movie>(`/movies/${editingId.value}`, payload)
      const idx = movies.value.findIndex(m => m.id === editingId.value)
      if (idx !== -1) movies.value[idx] = data
    } else {
      const { data } = await api.post<Movie>('/movies', payload)
      movies.value.unshift(data)
    }
    closeForm()
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Błąd zapisu.'
  }
}

async function deleteMovie(m: Movie) {
  if (!window.confirm(`Usunąć film „${m.title}"?`)) return
  try {
    await api.delete(`/movies/${m.id}`)
    movies.value = movies.value.filter(x => x.id !== m.id)
  } catch { error.value = 'Nie udało się usunąć filmu.' }
}

onMounted(fetchMovies)
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Filmy</h1>
      <Button size="sm" @click="openCreate">
        <Plus class="w-4 h-4 mr-1" /> Dodaj film
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
          <CardTitle class="text-base">{{ editingId ? 'Edytuj film' : 'Nowy film' }}</CardTitle>
          <Button variant="ghost" size="sm" @click="closeForm"><X class="w-4 h-4" /></Button>
        </div>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1 md:col-span-2">
            <Label>Tytuł *</Label>
            <Input v-model="form.title" placeholder="Tytuł filmu" />
          </div>
          <div class="space-y-1 md:col-span-2">
            <Label>Opis</Label>
            <Input v-model="form.description" placeholder="Krótki opis" />
          </div>
          <div class="space-y-1">
            <Label>Czas trwania (min) *</Label>
            <Input v-model="form.durationMinutes" type="number" min="1" placeholder="120" />
          </div>
          <div class="space-y-1">
            <Label>Kategoria wiekowa *</Label>
            <Input v-model="form.ageRating" placeholder="PG-13" />
          </div>
          <div class="space-y-1">
            <Label>Język *</Label>
            <Input v-model="form.language" placeholder="Angielski" />
          </div>
          <div class="space-y-1">
            <Label>Napisy</Label>
            <Input v-model="form.subtitles" placeholder="Polski" />
          </div>
          <div class="space-y-1">
            <Label>Gatunek *</Label>
            <Input v-model="form.genre" placeholder="Akcja" />
          </div>
          <div class="space-y-1">
            <Label>URL plakatu</Label>
            <Input v-model="form.posterUrl" placeholder="https://..." />
          </div>
        </div>
        <div class="flex gap-2 mt-4">
          <Button @click="saveMovie">{{ editingId ? 'Zapisz zmiany' : 'Dodaj' }}</Button>
          <Button variant="outline" @click="closeForm">Anuluj</Button>
        </div>
      </CardContent>
    </Card>

    <!-- Table -->
    <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="space-y-3 p-4">
          <div v-for="i in 5" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>Tytuł</TableHead>
              <TableHead>Gatunek</TableHead>
              <TableHead>Czas</TableHead>
              <TableHead>Wiek</TableHead>
              <TableHead>Język</TableHead>
              <TableHead>Status</TableHead>
              <TableHead class="text-right">Akcje</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="movies.length === 0">Brak filmów</TableEmpty>
            <TableRow v-for="m in movies" :key="m.id">
              <TableCell class="font-medium">{{ m.title }}</TableCell>
              <TableCell class="text-muted-foreground">{{ m.genre }}</TableCell>
              <TableCell class="tabular-nums">{{ m.durationMinutes }} min</TableCell>
              <TableCell>{{ m.ageRating }}</TableCell>
              <TableCell class="text-muted-foreground">{{ m.language }}</TableCell>
              <TableCell>
                <Badge :variant="m.isActive ? 'default' : 'secondary'">
                  {{ m.isActive ? 'Aktywny' : 'Nieaktywny' }}
                </Badge>
              </TableCell>
              <TableCell class="text-right">
                <div class="flex gap-1 justify-end">
                  <Button variant="ghost" size="sm" @click="openEdit(m)">
                    <Pencil class="w-4 h-4" />
                  </Button>
                  <Button variant="ghost" size="sm" class="text-destructive hover:text-destructive" @click="deleteMovie(m)">
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
