package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.EmployeeReservationDto;
import com.goldencinema.backend.dto.ReservationVerificationDto;
import com.goldencinema.backend.dto.UpdateReservationStatusRequest;
import com.goldencinema.backend.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/reservations")
    public List<EmployeeReservationDto> getReservations(
            @RequestParam(required = false) Long screeningId) {
        return employeeService.getReservations(screeningId);
    }

    @GetMapping("/reservations/verify/{code}")
    public ReservationVerificationDto verifyReservation(@PathVariable String code) {
        return employeeService.verifyByCode(code);
    }

    @PatchMapping("/reservations/{id}/status")
    public EmployeeReservationDto updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateReservationStatusRequest request,
            Authentication authentication) {
        return employeeService.updateStatus(id, request.status(), authentication.getName());
    }
}