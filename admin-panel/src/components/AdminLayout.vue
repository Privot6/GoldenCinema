<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Film, LogOut, LayoutDashboard, Users, Ticket, Building2 } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen bg-background flex flex-col">
    <!-- Navbar -->
    <header class="sticky top-0 z-10 border-b border-border bg-card/80 backdrop-blur-sm">
      <div class="px-4 sm:px-6 h-16 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary">
            <Film class="w-4 h-4 text-primary-foreground" />
          </div>
          <span class="font-semibold text-foreground">Golden Cinema</span>
          <span class="text-muted-foreground text-sm hidden sm:inline">/ Admin</span>
        </div>
        <Button variant="ghost" size="sm" @click="handleLogout" class="gap-2 text-muted-foreground hover:text-foreground">
          <LogOut class="w-4 h-4" />
          <span class="hidden sm:inline">Wyloguj</span>
        </Button>
      </div>
    </header>

    <div class="flex flex-1">
      <!-- Sidebar -->
      <aside class="w-64 border-r border-border bg-card hidden md:block">
        <nav class="p-4 space-y-2">
          <RouterLink to="/dashboard" class="flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors text-muted-foreground hover:bg-secondary hover:text-foreground" active-class="bg-secondary text-secondary-foreground" exact-active-class="bg-secondary text-secondary-foreground">
            <LayoutDashboard class="w-5 h-5" />
            Dashboard
          </RouterLink>
          <RouterLink to="/movies" class="flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors text-muted-foreground hover:bg-secondary hover:text-foreground" active-class="bg-secondary text-secondary-foreground">
            <Film class="w-5 h-5" />
            Filmy
          </RouterLink>
          <RouterLink to="/halls" class="flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors text-muted-foreground hover:bg-secondary hover:text-foreground" active-class="bg-secondary text-secondary-foreground">
            <Building2 class="w-5 h-5" />
            Sale kinowe
          </RouterLink>
          <RouterLink to="/screenings" class="flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors text-muted-foreground hover:bg-secondary hover:text-foreground" active-class="bg-secondary text-secondary-foreground">
            <Ticket class="w-5 h-5" />
            Seanse
          </RouterLink>
          <RouterLink to="/users" class="flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors text-muted-foreground hover:bg-secondary hover:text-foreground" active-class="bg-secondary text-secondary-foreground">
            <Users class="w-5 h-5" />
            Użytkownicy
          </RouterLink>
        </nav>
      </aside>

      <!-- Main content -->
      <main class="flex-1 overflow-auto">
        <RouterView />
      </main>
    </div>
  </div>
</template>

