package com.goldencinema.backend.dto;

import java.util.List;

public class CinemaHallLayoutResponse {

    private Long id;
    private String name;
    private List<SeatGridItemDto> seats;

    public CinemaHallLayoutResponse() {}

    public CinemaHallLayoutResponse(Long id, String name, List<SeatGridItemDto> seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SeatGridItemDto> getSeats() { return seats; }
    public void setSeats(List<SeatGridItemDto> seats) { this.seats = seats; }
}
