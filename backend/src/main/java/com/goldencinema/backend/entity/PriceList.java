package com.goldencinema.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Encja cennika — definiuje mnożnik ceny dla danego typu biletu. */
@Entity
@Table(name = "price_list")
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, unique = true)
    private TicketType ticketType;

    @Column(name = "price_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal priceMultiplier;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public PriceList() {
    }

    public Long getId() {
        return id;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
