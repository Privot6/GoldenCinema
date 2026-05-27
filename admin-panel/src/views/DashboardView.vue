<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle, Building2, CalendarClock, FileBarChart, RefreshCw, Ticket, TrendingUp, Users } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

interface Movie { id: number; title: string }
interface Hall  { id: number; name: string }
interface Screening {
  id: number
  movie: Movie
  hall: Hall
  startTime: string
  endTime: string
  basePrice: number
  status: 'ZAPLANOWANY' | 'ANULOWANY' | 'ZAKONCZONY'
}
interface Stats {
  totalUsers: number
  todayScreenings: number
  upcomingScreenings: number
  totalHalls: number
  monthlyRevenue: number
}

const screenings = ref<Screening[]>([])
const stats = ref<Stats | null>(null)
const loading = ref(false)
const statsLoading = ref(false)
const error = ref('')

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

function formatPrice(price: number) {
  return new Intl.NumberFormat('pl-PL', { style: 'currency', currency: 'PLN' }).format(price)
}

async function fetchScreenings() {
  loading.value = true
  error.value = ''
  try {
    const response = await api.get<Screening[]>('/screenings', {
      params: { sort: 'startTime,asc' }
    })
    screenings.value = response.data
  } catch {
    error.value = 'Nie udało się pobrać listy seansów. Sprawdź połączenie z serwerem.'
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  statsLoading.value = true
  try {
    const { data } = await api.get<Stats>('/admin/stats')
    stats.value = data
  } catch {
    // non-fatal
  } finally {
    statsLoading.value = false
  }
}

function fetchAll() { Promise.all([fetchScreenings(), fetchStats()]) }

onMounted(fetchAll)
</script>

<template>
  <div class="px-4 sm:px-6 py-8 flex flex-col gap-6">
    <!-- Page header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <CalendarClock class="w-6 h-6 text-primary" />
        <div>
          <h1 class="text-xl font-semibold text-foreground">Dashboard</h1>
          <p class="text-sm text-muted-foreground">Przegląd systemu</p>
        </div>
      </div>
      <Button variant="outline" size="sm" @click="fetchAll" :disabled="loading || statsLoading" class="gap-2">
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading || statsLoading }" />
        Odśwież
      </Button>
    </div>

    <!-- Stats cards -->
    <div class="grid grid-cols-2 lg:grid-cols-5 gap-4">
      <template v-if="statsLoading">
        <div v-for="i in 5" :key="i" class="h-28 rounded-lg bg-secondary/50 animate-pulse" />
      </template>
      <template v-else-if="stats">
        <Card>
          <CardHeader class="pb-2">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">Użytkownicy</CardTitle>
              <Users class="w-4 h-4 text-muted-foreground" />
            </div>
          </CardHeader>
          <CardContent class="pt-0">
            <p class="text-2xl font-bold">{{ stats.totalUsers }}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader class="pb-2">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">Seanse dzisiaj</CardTitle>
              <CalendarClock class="w-4 h-4 text-muted-foreground" />
            </div>
          </CardHeader>
          <CardContent class="pt-0">
            <p class="text-2xl font-bold">{{ stats.todayScreenings }}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader class="pb-2">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">Nadchodzące (7 dni)</CardTitle>
              <Ticket class="w-4 h-4 text-muted-foreground" />
            </div>
          </CardHeader>
          <CardContent class="pt-0">
            <p class="text-2xl font-bold">{{ stats.upcomingScreenings }}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader class="pb-2">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">Sale kinowe</CardTitle>
              <Building2 class="w-4 h-4 text-muted-foreground" />
            </div>
          </CardHeader>
          <CardContent class="pt-0">
            <p class="text-2xl font-bold">{{ stats.totalHalls }}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader class="pb-2">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">Przychód (mies.)</CardTitle>
              <TrendingUp class="w-4 h-4 text-muted-foreground" />
            </div>
          </CardHeader>
          <CardContent class="pt-0">
            <p class="text-2xl font-bold">{{ formatPrice(stats.monthlyRevenue) }}</p>
          </CardContent>
        </Card>
      </template>
    </div>

    <Card>
      <CardHeader class="pb-3">
        <div class="flex items-center justify-between">
          <div>
            <CardTitle class="text-base">Raporty finansowe</CardTitle>
            <CardDescription>Generuj raporty PDF z podziałem na filmy i seanse</CardDescription>
          </div>
          <FileBarChart class="w-5 h-5 text-muted-foreground" />
        </div>
      </CardHeader>
      <CardContent>
        <RouterLink to="/reports">
          <Button variant="outline" class="gap-2">
            <FileBarChart class="w-4 h-4" />
            Otwórz raporty
          </Button>
        </RouterLink>
      </CardContent>
    </Card>

    <!-- Error -->
    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <!-- Screenings table -->
    <Card>
      <CardHeader class="pb-3">
        <CardTitle class="text-base">Nadchodzące seanse</CardTitle>
        <CardDescription>
          {{ screenings.length > 0 ? `${screenings.length} seansów` : 'Brak danych' }}
        </CardDescription>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Film</TableHead>
              <TableHead>Sala</TableHead>
              <TableHead>Początek</TableHead>
              <TableHead>Koniec</TableHead>
              <TableHead>Cena</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!loading && screenings.length === 0">
              <span class="text-muted-foreground text-sm">Brak seansów do wyświetlenia</span>
            </TableEmpty>
            <TableRow v-for="s in screenings" :key="s.id">
              <TableCell class="font-medium text-foreground">{{ s.movie?.title ?? '—' }}</TableCell>
              <TableCell class="text-muted-foreground">{{ s.hall?.name ?? '—' }}</TableCell>
              <TableCell class="text-muted-foreground tabular-nums">{{ formatDate(s.startTime) }}</TableCell>
              <TableCell class="text-muted-foreground tabular-nums">{{ formatDate(s.endTime) }}</TableCell>
              <TableCell class="tabular-nums">{{ formatPrice(s.basePrice) }}</TableCell>
              <TableCell>
                <Badge :variant="statusVariant(s.status)">{{ statusLabel(s.status) }}</Badge>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <div v-if="loading" class="space-y-3 p-4">
          <div v-for="i in 5" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
        </div>
      </CardContent>
    </Card>

  </div>
</template>
