
GoldenCinema – system rezerwacji miejsc w kinie

Opis

GoldenCinema to aplikacja mobilna umożliwiająca użytkownikom przegląd repertuaru kina, wybór seansu oraz rezerwację konkretnych miejsc na sali. System uwzględnia wiele sal i stały układ miejsc. Pracownicy kina mogą zarządzać rezerwacjami (potwierdzać/anulować), a administrator zarządza repertuarem i salami.

Problem

Rezerwacje są często rozproszone (telefon, kasa, zewnętrzne strony). Brakuje prostego systemu, który:
pokazuje dostępność miejsc w czasie rzeczywistym,
porządkuje statusy rezerwacji.

Grupa docelowa

Klienci kina
Pracownicy kina
Manager/Admin

Role

CLIENT – rezerwuje miejsca i przegląda swoje rezerwacje
EMPLOYEE – obsługuje rezerwacje (potwierdzanie/anulowanie)
ADMIN – zarządza salami, seansami, filmami, cenami i raportami

Stos technologiczny

Android: Kotlin
Backend: Java + Spring Boot (REST API)
DB: PostgreSQL
Auth: JWT
Kontenery: Docker Compose (backend + DB)

Raporty: generowanie PDF po stronie backendu
