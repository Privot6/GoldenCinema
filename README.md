# GoldenCinema – system rezerwacji miejsc w kinie

## Opis

GoldenCinema to aplikacja mobilna umożliwiająca użytkownikom przegląd repertuaru kina, wybór seansu oraz rezerwację konkretnych miejsc na sali. System uwzględnia wiele sal i stały układ miejsc. Pracownicy kina mogą zarządzać rezerwacjami (potwierdzać/anulować), a administrator zarządza repertuarem i salami.

## Wizja projektu

Celem projektu GoldenCinema jest stworzenie prostego i wygodnego systemu rezerwacji miejsc w kinie dostępnego z poziomu aplikacji mobilnej. System ma umożliwić użytkownikom szybkie sprawdzanie repertuaru, wybór konkretnych miejsc na sali oraz zarządzanie rezerwacjami.

Dodatkowo aplikacja ma usprawnić pracę obsługi kina poprzez panel do zarządzania rezerwacjami oraz umożliwić administratorowi kontrolę nad repertuarem, salami i cenami biletów.

## Problem

Rezerwacje są często rozproszone (telefon, kasa, zewnętrzne strony). Brakuje prostego systemu, który:

* pokazuje dostępność miejsc w czasie rzeczywistym,
* porządkuje statusy rezerwacji.

## Grupa docelowa

* Klienci kina
* Pracownicy kina
* Manager/Admin

## Role

**CLIENT** – rezerwuje miejsca i przegląda swoje rezerwacje
**EMPLOYEE** – obsługuje rezerwacje (potwierdzanie/anulowanie)
**ADMIN** – zarządza salami, seansami, filmami, cenami i raportami

## MVP

Minimalna wersja systemu (Minimum Viable Product) będzie zawierać podstawowe funkcjonalności umożliwiające korzystanie z systemu rezerwacji:

* Rejestracja i logowanie użytkownika (JWT)

* Przegląd repertuaru: lista filmów i seansów

* Wyświetlanie szczegółów seansu

* Widok sali z mapą miejsc (wolne / zajęte)

* Rezerwacja wybranych miejsc

* Statusy rezerwacji:

  * NEW
  * CONFIRMED
  * CANCELLED
  * EXPIRED

* Panel pracownika:

  * lista rezerwacji
  * potwierdzanie rezerwacji
  * anulowanie rezerwacji

## Stos technologiczny

Android: Kotlin
Backend: Java + Spring Boot (REST API)
DB: PostgreSQL
Auth: JWT
Kontenery: Docker Compose (backend + DB)

Raporty: generowanie PDF po stronie backendu

## Strategia branchowania

W projekcie stosujemy prostą strategię branchowania opartą na modelu zbliżonym do GitFlow.

### Główne gałęzie

**main**
Stabilna wersja projektu. Zawiera tylko sprawdzony kod gotowy do prezentacji lub wydania.

**develop**
Gałąź integracyjna, na której łączone są nowe funkcjonalności. Jest główną gałęzią do bieżącego rozwoju projektu.

### Gałęzie funkcjonalne

Nowe funkcjonalności tworzone są w osobnych gałęziach tworzonych z `develop`.

Format nazwy:

```
feature/nazwa-funkcji
```

Przykłady:

```
feature/auth-login
feature/seat-reservation
feature/showtimes-list
```

Po zakończeniu pracy gałąź `feature` jest łączona z `develop` poprzez Pull Request.

### Poprawki błędów

Drobne poprawki błędów tworzone są jako:

```
hotfix/nazwa-poprawki
```

Przykłady:

```
hotfix/reservation-validation
hotfix/login-error
```

Hotfixy mogą być łączone z `develop`, a w razie potrzeby również z `main`.

### Workflow pracy

1. Tworzenie gałęzi `feature` z `develop`
2. Implementacja funkcjonalności
3. Commit zmian zgodnie z przyjętą konwencją commitów
4. Utworzenie Pull Request do `develop`
5. Code review
6. Merge do `develop`

Gałąź `main` aktualizowana jest tylko stabilnymi wersjami projektu.

## Struktura projektu

Przykładowa struktura repozytorium GoldenCinema (na start):

/GoldenCinema
├── backend/           # kod backendu (Spring Boot)
├── mobile/            # aplikacja Android (Kotlin)
├── docs/              # dokumentacja, diagramy, raporty
├── docker-compose.yml # konfiguracja kontenerów
└── README.md          # opis projektu

Ta struktura pozwala na jasny podział kodu i dokumentacji oraz przygotowuje repozytorium do pracy zespołowej.

