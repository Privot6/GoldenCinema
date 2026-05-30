<script setup lang="ts">
import { XCircle, Film } from 'lucide-vue-next'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background p-4">
    <div class="w-full max-w-sm space-y-6">
      <div class="flex flex-col items-center gap-2">
        <div class="flex items-center justify-center w-14 h-14 rounded-2xl bg-primary shadow-lg shadow-primary/30">
          <Film class="w-7 h-7 text-primary-foreground" />
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-foreground">Golden Cinema</h1>
      </div>

      <Card>
        <CardHeader class="pb-4 items-center text-center">
          <XCircle class="w-12 h-12 text-red-500 mb-2" />
          <CardTitle class="text-lg">Płatność anulowana</CardTitle>
          <CardDescription>Płatność nie została zrealizowana. Rezerwacja pozostaje niezapłacona.</CardDescription>
        </CardHeader>
        <CardContent class="text-center text-sm text-muted-foreground">
          Możesz teraz zamknąć tę stronę.
        </CardContent>
      </Card>
    </div>
  </div>
</template>
