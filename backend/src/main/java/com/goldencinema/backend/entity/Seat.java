package com.goldencinema.backend.entity;

import jakarta.persistence.*;

/** Encja reprezentująca pojedyncze miejsce w sali kinowej wraz z jego pozycją w siatce. */
@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(name = "uq_seat", columnNames = {"hall_id", "row_label", "seat_number"}))
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private CinemaHall hall;

    @Column(name = "row_label", length = 10, nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "grid_row")
    private Integer gridRow;

    @Column(name = "grid_col")
    private Integer gridCol;

    public Seat() {
    }

    public Long getId() {
        return id;
    }

    public CinemaHall getHall() {
        return hall;
    }

    public void setHall(CinemaHall hall) {
        this.hall = hall;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public void setGridRow(Integer gridRow) {
        this.gridRow = gridRow;
    }

    public Integer getGridCol() {
        return gridCol;
    }

    public void setGridCol(Integer gridCol) {
        this.gridCol = gridCol;
    }
}
