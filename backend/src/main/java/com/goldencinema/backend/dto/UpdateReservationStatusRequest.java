package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ReservationStatus;

public record UpdateReservationStatusRequest(ReservationStatus status) {}
