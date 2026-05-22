<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { FileBarChart, FileDown, AlertCircle, Loader2 } from 'lucide-vue-next'

type ReportType = 'weekly' | 'monthly'

// ── helpers ─────────────────────────────────────────────────────────────
function isoDate(d: Date) {
  return d.toISOString().slice(0, 10)
}

function getMondayOfCurrentWeek() {
  const d = new Date()
  const day = d.getDay()
  d.setDate(d.getDate() - (day === 0 ? 6 : day - 1))
  return isoDate(d)
}

function lastDayOfMonth(year: number, month: number) {
  return new Date(year, month, 0).getDate()
}

// ── state ────────────────────────────────────────────────────────────────
const reportType = ref<ReportType>('weekly')

// weekly
const dateFrom = ref(getMondayOfCurrentWeek())
const dateTo   = ref(isoDate(new Date()))

// monthly – build last 18 months as options
interface MonthOption { value: string; label: string; from: string; to: string }
function buildMonthOptions(): MonthOption[] {
  const opts: MonthOption[] = []
  const MONTHS_PL = ['Styczeń','Luty','Marzec','Kwiecień','Maj','Czerwiec',
                     'Lipiec','Sierpień','Wrzesień','Październik','Listopad','Grudzień']
  const now = new Date()
  for (let i = 0; i < 18; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const y = d.getFullYear() as number
    const m = (d.getMonth() + 1) as number
    const mm = String(m).padStart(2, '0')
    const ld = lastDayOfMonth(y, m)
    opts.push({
      value: `${y}-${mm}`,
      label: `${MONTHS_PL[m - 1]} ${y}`,
      from:  `${y}-${mm}-01`,
      to:    `${y}-${mm}-${String(ld).padStart(2, '0')}`
    })
  }
  return opts
}
const monthOptions  = buildMonthOptions()
const selectedMonth = ref(monthOptions[0]!.value)

// ── computed dates ───────────────────────────────────────────────────────
const effectiveFrom = computed(() => {
  if (reportType.value === 'weekly') return dateFrom.value
  return monthOptions.find(o => o.value === selectedMonth.value)?.from ?? ''
})

const effectiveTo = computed(() => {
  if (reportType.value === 'weekly') return dateTo.value
  return monthOptions.find(o => o.value === selectedMonth.value)?.to ?? ''
})

const weeklyValid = computed(
  () => !!dateFrom.value && !!dateTo.value && dateTo.value >= dateFrom.value
)
const isValid = computed(() =>
  reportType.value === 'monthly' ? true : weeklyValid.value
)

// ── PDF ──────────────────────────────────────────────────────────────────
const loading = ref(false)
const error   = ref('')
const pdfUrl  = ref('')

onUnmounted(() => { if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value) })

async function generateReport() {
  if (!isValid.value || loading.value) return
  loading.value = true
  error.value   = ''
  if (pdfUrl.value) { URL.revokeObjectURL(pdfUrl.value); pdfUrl.value = '' }

  try {
    const token = localStorage.getItem('token')
    const params = new URLSearchParams({ from: effectiveFrom.value, to: effectiveTo.value })
    const res = await fetch(`/api/admin/reports/weekly-profit?${params}`, {
      headers: { Authorization: `Bearer ${token ?? ''}` }
    })

    if (!res.ok) {
      if (res.status === 401 || res.status === 403) {
        error.value = 'Brak autoryzacji. Odśwież stronę lub zaloguj się ponownie.'
      } else if (res.status === 400) {
        error.value = 'Nieprawidłowy zakres dat.'
      } else {
        error.value = `Błąd serwera (${res.status}). Sprawdź logi backendu.`
      }
      return
    }

    const blob = await res.blob()
    pdfUrl.value = URL.createObjectURL(blob)
  } catch {
    error.value = 'Nie udało się połączyć z serwerem.'
  } finally {
    loading.value = false
  }
}

function downloadPdf() {
  if (!pdfUrl.value) return
  const a = document.createElement('a')
  a.href = pdfUrl.value
  a.download = `raport_${effectiveFrom.value}_${effectiveTo.value}.pdf`
  a.click()
}
</script>

<template>
  <div class="px-4 sm:px-6 py-8 flex flex-col gap-6 max-w-5xl">

    <!-- Header -->
    <div class="flex items-center gap-3">
      <FileBarChart class="w-6 h-6 text-primary" />
      <div>
        <h1 class="text-xl font-semibold text-foreground">Raporty</h1>
        <p class="text-sm text-muted-foreground">Generuj raporty PDF z przychodem kina</p>
      </div>
    </div>

    <!-- Controls -->
    <Card>
      <CardHeader class="pb-4">
        <CardTitle class="text-base">Parametry raportu</CardTitle>
        <CardDescription>Wybierz typ i zakres dat, a następnie kliknij „Generuj PDF"</CardDescription>
      </CardHeader>
      <CardContent class="space-y-5">

        <!-- Type toggle -->
        <div class="space-y-1.5">
          <Label>Typ raportu</Label>
          <div class="flex rounded-lg border border-border bg-muted p-1 gap-1 w-fit">
            <button
              @click="reportType = 'weekly'"
              :class="reportType === 'weekly'
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'"
              class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            >
              Tygodniowy
            </button>
            <button
              @click="reportType = 'monthly'"
              :class="reportType === 'monthly'
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'"
              class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            >
              Miesięczny
            </button>
          </div>
        </div>

        <!-- Weekly: from / to -->
        <div v-if="reportType === 'weekly'" class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <Label for="dateFrom">Od</Label>
            <Input id="dateFrom" type="date" v-model="dateFrom" />
          </div>
          <div class="space-y-1.5">
            <Label for="dateTo">Do</Label>
            <Input id="dateTo" type="date" v-model="dateTo" />
          </div>
          <p v-if="!weeklyValid && dateFrom && dateTo" class="col-span-2 text-sm text-destructive -mt-2">
            Data „do" nie może być wcześniejsza niż data „od".
          </p>
        </div>

        <!-- Monthly: month selector -->
        <div v-else class="space-y-1.5">
          <Label for="monthSelect">Miesiąc</Label>
          <select
            id="monthSelect"
            v-model="selectedMonth"
            class="flex h-9 w-56 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          >
            <option v-for="opt in monthOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- Error -->
        <Alert v-if="error" variant="destructive">
          <AlertCircle class="w-4 h-4" />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <!-- Actions -->
        <div class="flex gap-3">
          <Button :disabled="!isValid || loading" @click="generateReport" class="gap-2">
            <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
            <FileBarChart v-else class="w-4 h-4" />
            {{ loading ? 'Generowanie…' : 'Generuj PDF' }}
          </Button>
          <Button v-if="pdfUrl" variant="outline" @click="downloadPdf" class="gap-2">
            <FileDown class="w-4 h-4" />
            Pobierz PDF
          </Button>
        </div>
      </CardContent>
    </Card>

    <!-- PDF preview -->
    <Card v-if="pdfUrl">
      <CardHeader class="pb-3">
        <CardTitle class="text-base">Podgląd raportu</CardTitle>
        <CardDescription>
          Zakres: {{ effectiveFrom }} — {{ effectiveTo }}
        </CardDescription>
      </CardHeader>
      <CardContent class="p-0 overflow-hidden rounded-b-lg">
        <iframe
          :src="pdfUrl"
          class="w-full border-0"
          style="height: 680px"
          title="Podgląd raportu PDF"
        />
      </CardContent>
    </Card>

  </div>
</template>
