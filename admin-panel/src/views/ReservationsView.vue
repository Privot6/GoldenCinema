<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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

const PAGE_SIZE = 25
const page = ref(1)
const paged = computed(() => reservations.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
const totalPages = computed(() => Math.max(1, Math.ceil(reservations.value.length / PAGE_SIZE)))

async function fetchReservations() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<AdminReservation[]>('/admin/reservations')
    reservations.value = data
    page.value = 1
  } catch {
    error.value = 'Nie udało się pobrać listy rezerwacji.'
  } finally {
    loading.value = false
  }
}

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

onMounted(fetchReservations)
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Rezerwacje</h1>
      <Button variant="outline" size="sm" @click="fetchReservations" :disabled="loading" class="gap-2">
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
          {{ reservations.length > 0 ? `${reservations.length} rezerwacji` : 'Brak rezerwacji' }}
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
            <TableRow v-for="r in paged" :key="r.id">
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
          <span class="text-sm text-muted-foreground">Strona {{ page }} z {{ totalPages }} ({{ reservations.length }} rezerwacji)</span>
          <div class="flex gap-2">
            <Button variant="outline" size="sm" :disabled="page === 1" @click="page--">Poprzednia</Button>
            <Button variant="outline" size="sm" :disabled="page === totalPages" @click="page++">Następna</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
