package com.goldencinema.backend.entity;

/**
 * Status rezerwacji biletu.
 */
public enum ReservationStatus {
    /** Rezerwacja utworzona, płatność jeszcze nie zrealizowana. */
    OCZEKUJACA,
    /** Płatność potwierdzona — bilet jest ważny. */
    POTWIERDZONA,
    /** Rezerwacja anulowana przez klienta, pracownika lub administratora. */
    ANULOWANA,
    /** Sesja płatności wygasła bez opłacenia. */
    WYGASLA
}
