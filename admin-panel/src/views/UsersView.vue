<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import api from '@/api/axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Table, TableBody, TableCell, TableEmpty,
  TableHead, TableHeader, TableRow
} from '@/components/ui/table'
import { AlertCircle, Plus, Trash2, X } from 'lucide-vue-next'

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
const showForm = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

interface PagedResponse<T> { content: T[]; page: number; totalPages: number; totalElements: number }

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  role: 'USER'
})

function resetForm() {
  form.firstName = ''; form.lastName = ''; form.email = ''
  form.phone = ''; form.password = ''; form.role = 'USER'
}

function openCreate() { resetForm(); showForm.value = true }
function closeForm() { showForm.value = false; resetForm() }

async function fetchUsers(p = currentPage.value) {
  loading.value = true; error.value = ''
  try {
    const { data } = await api.get<PagedResponse<UserSummary>>('/admin/users', { params: { page: p, size: 20 } })
    users.value = data.content
    currentPage.value = data.page
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch { error.value = 'Nie udało się pobrać listy użytkowników.' }
  finally { loading.value = false }
}

function prevPage() { if (currentPage.value > 0) fetchUsers(currentPage.value - 1) }
function nextPage() { if (currentPage.value < totalPages.value - 1) fetchUsers(currentPage.value + 1) }

async function saveUser() {
  error.value = ''
  try {
    await api.post<UserSummary>('/admin/users', {
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      phone: form.phone || null,
      password: form.password,
      role: form.role
    })
    await fetchUsers(0)
    closeForm()
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Błąd podczas tworzenia użytkownika.'
  }
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

async function deleteUser(user: UserSummary) {
  if (!window.confirm(`Usunąć użytkownika ${user.firstName} ${user.lastName}?`)) return
  try {
    await api.delete(`/admin/users/${user.id}`)
    await fetchUsers(currentPage.value)
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Nie udało się usunąć użytkownika.'
  }
}

onMounted(() => fetchUsers(0))
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Użytkownicy</h1>
      <Button size="sm" @click="openCreate">
        <Plus class="w-4 h-4 mr-1" /> Nowy użytkownik
      </Button>
    </div>

    <Alert v-if="error" variant="destructive">
      <AlertCircle class="w-4 h-4" />
      <AlertDescription>{{ error }}</AlertDescription>
    </Alert>

    <!-- Create form -->
    <Card v-if="showForm">
      <CardHeader class="pb-3">
        <div class="flex items-center justify-between">
          <CardTitle class="text-base">Nowy użytkownik</CardTitle>
          <Button variant="ghost" size="sm" @click="closeForm"><X class="w-4 h-4" /></Button>
        </div>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <Label>Imię *</Label>
            <Input v-model="form.firstName" placeholder="Jan" />
          </div>
          <div class="space-y-1">
            <Label>Nazwisko *</Label>
            <Input v-model="form.lastName" placeholder="Kowalski" />
          </div>
          <div class="space-y-1">
            <Label>Email *</Label>
            <Input v-model="form.email" type="email" placeholder="jan@example.com" />
          </div>
          <div class="space-y-1">
            <Label>Telefon</Label>
            <Input v-model="form.phone" placeholder="123456789" />
          </div>
          <div class="space-y-1">
            <Label>Hasło *</Label>
            <Input v-model="form.password" type="password" placeholder="••••••" />
          </div>
          <div class="space-y-1">
            <Label>Rola *</Label>
            <select v-model="form.role"
              class="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm">
              <option v-for="r in ROLES" :key="r" :value="r">{{ r }}</option>
            </select>
          </div>
        </div>
        <div class="flex gap-2 mt-4">
          <Button @click="saveUser">Utwórz</Button>
          <Button variant="outline" @click="closeForm">Anuluj</Button>
        </div>
      </CardContent>
    </Card>

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
              <TableHead class="text-right">Akcje</TableHead>
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
              <TableCell class="text-right">
                <Button
                  variant="ghost"
                  size="sm"
                  class="text-destructive hover:text-destructive"
                  @click="deleteUser(u)"
                >
                  <Trash2 class="w-4 h-4" />
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t">
          <span class="text-sm text-muted-foreground">Strona {{ currentPage + 1 }} z {{ totalPages }} ({{ totalElements }} użytkowników)</span>
          <div class="flex gap-2">
            <Button variant="outline" size="sm" :disabled="currentPage === 0" @click="prevPage">Poprzednia</Button>
            <Button variant="outline" size="sm" :disabled="currentPage >= totalPages - 1" @click="nextPage">Następna</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
