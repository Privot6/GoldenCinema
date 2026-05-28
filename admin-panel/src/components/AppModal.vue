<script setup lang="ts">
defineProps<{ open: boolean; title: string }>()
defineEmits<{ close: [] }>()
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @keydown.esc="$emit('close')"
      >
        <!-- backdrop -->
        <div
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          @click="$emit('close')"
        />
        <!-- panel -->
        <div
          class="relative z-10 w-full max-w-2xl rounded-xl bg-background shadow-2xl border flex flex-col max-h-[90vh]"
        >
          <!-- header -->
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <h2 class="text-base font-semibold">{{ title }}</h2>
            <button
              type="button"
              @click="$emit('close')"
              class="rounded-md p-1 text-muted-foreground hover:bg-secondary hover:text-foreground transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-4 h-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M18 6 6 18" /><path d="m6 6 12 12" />
              </svg>
            </button>
          </div>
          <!-- body -->
          <div class="overflow-y-auto px-6 py-6">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.15s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
