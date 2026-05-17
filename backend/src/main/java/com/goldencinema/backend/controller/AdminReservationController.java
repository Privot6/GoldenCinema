package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.AdminReservationDto;
import com.goldencinema.backend.dto.UpdateReservationStatusRequest;
import com.goldencinema.backend.service.AdminReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping
    public List<AdminReservationDto> getAllReservations() {
        return adminReservationService.getAllReservations();
    }

    @PutMapping("/{id}/status")
    public AdminReservationDto updateStatus(@PathVariable Long id,
                                            @RequestBody UpdateReservationStatusRequest request,
                                            Authentication authentication) {
        return adminReservationService.updateStatus(id, request.status(), authentication.getName());
    }
}
