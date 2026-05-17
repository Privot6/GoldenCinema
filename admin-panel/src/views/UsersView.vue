<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { Card, CardContent } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle } from 'lucide-vue-next'

interface UserSummary {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string
  isActive: boolean
  role: string
}

const ROLES = ['USER', 'EMPLOYEE', 'ADMIN']

const users = ref<UserSummary[]>([])
const loading = ref(false)
const error = ref('')

async function fetchUsers() {
  loading.value = true; error.value = ''
  try {
    const { data } = await api.get<UserSummary[]>('/admin/users')
    users.value = data
  } catch { error.value = 'Nie udało się pobrać listy użytkowników.' }
  finally { loading.value = false }
}

async function toggleActive(user: UserSummary) {
  try {
    const { data } = await api.put<UserSummary>(`/admin/users/${user.id}`, { isActive: !user.isActive })
    const idx = users.value.findIndex(u => u.id === user.id)
    if (idx !== -1) users.value[idx] = data
  } catch { error.value = 'Nie udało się zmienić statusu.' }
}

async function changeRole(user: UserSummary, role: string) {
  try {
    const { data } = await api.put<UserSummary>(`/admin/users/${user.id}`, { role })
    const idx = users.value.findIndex(u => u.id === user.id)
    if (idx !== -1) users.value[idx] = data
  } catch { error.value = 'Nie udało się zmienić roli.' }
}

function roleBadgeVariant(role: string) {
  if (role === 'ADMIN') return 'destructive'
  if (role === 'EMPLOYEE') return 'default'
  return 'secondary'
}

onMounted(fetchUsers)
</script>

<template>
  <div class="p-6 space-y-6">
    <h1 class="text-2xl font-bold">Użytkownicy</h1>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="space-y-3 p-4">
          <div v-for="i in 5" :key="i" class="h-10 rounded-md bg-secondary/50 animate-pulse" />
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>Imię i nazwisko</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Telefon</TableHead>
              <TableHead>Rola</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="users.length === 0">Brak użytkowników</TableEmpty>
            <TableRow v-for="u in users" :key="u.id">
              <TableCell class="font-medium">{{ u.firstName }} {{ u.lastName }}</TableCell>
              <TableCell class="text-muted-foreground">{{ u.email }}</TableCell>
              <TableCell class="text-muted-foreground">{{ u.phone ?? '—' }}</TableCell>
              <TableCell>
                <select
                  :value="u.role"
                  @change="changeRole(u, ($event.target as HTMLSelectElement).value)"
                  class="h-7 rounded-md border border-input bg-background px-2 text-xs shadow-sm"
                >
                  <option v-for="r in ROLES" :key="r" :value="r">{{ r }}</option>
                </select>
              </TableCell>
              <TableCell>
                <button
                  @click="toggleActive(u)"
                  class="inline-flex items-center gap-1.5 text-xs font-medium px-2 py-0.5 rounded-full transition-colors"
                  :class="u.isActive
                    ? 'bg-green-100 text-green-800 hover:bg-green-200'
                    : 'bg-red-100 text-red-800 hover:bg-red-200'"
                >
                  <span class="w-1.5 h-1.5 rounded-full" :class="u.isActive ? 'bg-green-500' : 'bg-red-500'" />
                  {{ u.isActive ? 'Aktywny' : 'Nieaktywny' }}
                </button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
