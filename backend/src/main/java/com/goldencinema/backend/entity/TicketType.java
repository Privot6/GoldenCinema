package com.goldencinema.backend.entity;

/**
 * Typ biletu — określa mnożnik ceny z cennika.
 */
public enum TicketType {
    /** Bilet normalny — pełna cena (mnożnik 1.0). */
    NORMALNY,
    /** Bilet ulgowy — obniżona cena (mnożnik 0.7). */
    ULGOWY
}
