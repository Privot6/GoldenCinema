<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle, Film, Lock, Mail } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

function validate() {
  if (!email.value.trim()) return 'Email jest wymagany.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) return 'Podaj poprawny adres email.'
  if (!password.value) return 'Hasło jest wymagane.'
  if (password.value.length < 3) return 'Hasło jest za krótkie.'
  return ''
}

async function handleLogin() {
  error.value = validate()
  if (error.value) return

  loading.value = true
  try {
    await authStore.login(email.value.trim(), password.value)
    router.push({ name: 'dashboard' })
  } catch (e: unknown) {
    const status = (e as { response?: { status?: number } })?.response?.status
    if (status === 401 || status === 403) {
      error.value = 'Nieprawidłowy email lub hasło.'
    } else {
      error.value = 'Błąd połączenia z serwerem. Spróbuj ponownie.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background p-4">
    <div class="w-full max-w-sm space-y-6">
      <!-- Logo -->
      <div class="flex flex-col items-center gap-2">
        <div class="flex items-center justify-center w-14 h-14 rounded-2xl bg-primary shadow-lg shadow-primary/30">
          <Film class="w-7 h-7 text-primary-foreground" />
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-foreground">
          Golden Cinema
        </h1>
        <p class="text-sm text-muted-foreground">Panel administracyjny</p>
      </div>

      <!-- Card -->
      <Card>
        <CardHeader class="pb-4">
          <CardTitle class="text-lg">Zaloguj się</CardTitle>
          <CardDescription>Wprowadź dane dostępowe konta admina</CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleLogin" class="space-y-4">
            <!-- Error alert -->
            <Alert v-if="error" variant="destructive">
              <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" />
              <AlertDescription>{{ error }}</AlertDescription>
            </Alert>

            <!-- Email -->
            <div class="space-y-1.5">
              <Label for="email">Email</Label>
              <div class="relative">
                <Mail class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                <Input
                  id="email"
                  v-model="email"
                  type="email"
                  placeholder="admin@goldencinema.pl"
                  autocomplete="email"
                  class="pl-9"
                  :disabled="loading"
                />
              </div>
            </div>

            <!-- Password -->
            <div class="space-y-1.5">
              <Label for="password">Hasło</Label>
              <div class="relative">
                <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
                <Input
                  id="password"
                  v-model="password"
                  type="password"
                  placeholder="••••••••"
                  autocomplete="current-password"
                  class="pl-9"
                  :disabled="loading"
                />
              </div>
            </div>

            <Button type="submit" class="w-full" size="lg" :disabled="loading">
              <span v-if="loading">Logowanie...</span>
              <span v-else>Zaloguj</span>
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
