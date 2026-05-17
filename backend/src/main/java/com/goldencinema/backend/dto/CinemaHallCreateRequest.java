package com.goldencinema.backend.dto;

import java.util.List;

public class CinemaHallCreateRequest {

    private String name;
    private List<SeatGridItemDto> seats;

    public CinemaHallCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SeatGridItemDto> getSeats() { return seats; }
    public void setSeats(List<SeatGridItemDto> seats) { this.seats = seats; }
}
