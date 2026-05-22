<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle, CheckCircle, RefreshCw, X } from 'lucide-vue-next'

interface AdminReservation {
  id: number
  reservationCode: string
  status: 'OCZEKUJACA' | 'POTWIERDZONA' | 'ANULOWANA'
  totalPrice: number
  createdAt: string
  userFirstName: string
  userLastName: string
  userEmail: string
  movieTitle: string
  hallName: string
  screeningStartTime: string
}

const reservations = ref<AdminReservation[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

interface PagedResponse<T> { content: T[]; page: number; totalPages: number; totalElements: number }

function statusVariant(status: AdminReservation['status']) {
  if (status === 'POTWIERDZONA') return 'secondary'
  if (status === 'ANULOWANA') return 'destructive'
  return 'default'
}

function statusLabel(status: AdminReservation['status']) {
  if (status === 'POTWIERDZONA') return 'Potwierdzona'
  if (status === 'ANULOWANA') return 'Anulowana'
  return 'Oczekująca'
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

async function fetchReservations(p = currentPage.value) {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<PagedResponse<AdminReservation>>('/admin/reservations', { params: { page: p, size: 20 } })
    reservations.value = data.content
    currentPage.value = data.page
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch {
    error.value = 'Nie udało się pobrać listy rezerwacji.'
  } finally {
    loading.value = false
  }
}

function prevPage() { if (currentPage.value > 0) fetchReservations(currentPage.value - 1) }
function nextPage() { if (currentPage.value < totalPages.value - 1) fetchReservations(currentPage.value + 1) }

async function updateStatus(reservation: AdminReservation, status: 'POTWIERDZONA' | 'ANULOWANA') {
  error.value = ''
  try {
    const { data } = await api.put<AdminReservation>(`/admin/reservations/${reservation.id}/status`, { status })
    const idx = reservations.value.findIndex(r => r.id === reservation.id)
    if (idx !== -1) reservations.value[idx] = data
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Nie udało się zmienić statusu rezerwacji.'
  }
}

onMounted(() => fetchReservations(0))
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Rezerwacje</h1>
      <Button variant="outline" size="sm" @click="fetchReservations(currentPage)" :disabled="loading" class="gap-2">
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        Odśwież
      </Button>
    </div>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <Card>
      <CardHeader class="pb-3">
        <CardTitle class="text-base">
          {{ totalElements > 0 ? `${totalElements} rezerwacji` : 'Brak rezerwacji' }}
        </CardTitle>
      </CardHeader>
      <CardContent class="p-0">
        <div v-if="loading" class="space-y-3 p-4">
          <div v-for="i in 5" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>Kod</TableHead>
              <TableHead>Użytkownik</TableHead>
              <TableHead>Film / Sala</TableHead>
              <TableHead>Data seansu</TableHead>
              <TableHead>Cena</TableHead>
              <TableHead>Status</TableHead>
              <TableHead class="text-right">Akcje</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="reservations.length === 0">Brak rezerwacji</TableEmpty>
            <TableRow v-for="r in reservations" :key="r.id">
              <TableCell class="font-mono text-sm">{{ r.reservationCode }}</TableCell>
              <TableCell>
                <div class="font-medium">{{ r.userFirstName }} {{ r.userLastName }}</div>
                <div class="text-xs text-muted-foreground">{{ r.userEmail }}</div>
              </TableCell>
              <TableCell>
                <div class="font-medium">{{ r.movieTitle }}</div>
                <div class="text-xs text-muted-foreground">{{ r.hallName }}</div>
              </TableCell>
              <TableCell class="tabular-nums text-muted-foreground">{{ formatDate(r.screeningStartTime) }}</TableCell>
              <TableCell class="tabular-nums">{{ formatPrice(r.totalPrice) }}</TableCell>
              <TableCell>
                <Badge :variant="statusVariant(r.status)">{{ statusLabel(r.status) }}</Badge>
              </TableCell>
              <TableCell class="text-right">
                <div class="flex items-center justify-end gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    class="text-green-600 hover:text-green-700"
                    :disabled="r.status !== 'OCZEKUJACA'"
                    @click="updateStatus(r, 'POTWIERDZONA')"
                  >
                    <CheckCircle class="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    class="text-destructive hover:text-destructive"
                    :disabled="r.status === 'ANULOWANA'"
                    @click="updateStatus(r, 'ANULOWANA')"
                  >
                    <X class="w-4 h-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t">
          <span class="text-sm text-muted-foreground">Strona {{ currentPage + 1 }} z {{ totalPages }} ({{ totalElements }} rezerwacji)</span>
          <div class="flex gap-2">
            <Button variant="outline" size="sm" :disabled="currentPage === 0" @click="prevPage">Poprzednia</Button>
            <Button variant="outline" size="sm" :disabled="currentPage >= totalPages - 1" @click="nextPage">Następna</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
