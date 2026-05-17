package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.UpdateUserRequest;
import com.goldencinema.backend.dto.UserSummaryDto;
import com.goldencinema.backend.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<UserSummaryDto> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @PutMapping("/{id}")
    public UserSummaryDto updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return adminUserService.updateUser(id, request);
    }
}
