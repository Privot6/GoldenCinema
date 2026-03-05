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
