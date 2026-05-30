package com.goldencinema.backend.entity;

/**
 * Status seansu kinowego.
 */
public enum ScreeningStatus {
    /** Seans zaplanowany — bilety można rezerwować. */
    ZAPLANOWANY,
    /** Seans anulowany przez administratora. */
    ANULOWANY,
    /** Seans zakończony. */
    ZAKONCZONY
}
