package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.UpdateUserRequest;
import com.goldencinema.backend.dto.UserSummaryDto;
import com.goldencinema.backend.entity.Role;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.repository.RoleRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public UserSummaryDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + request.getRole()));
            user.setRoles(Set.of(role));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toDto(userRepository.save(user));
    }

    private UserSummaryDto toDto(User user) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");
        return new UserSummaryDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPhone(), user.getIsActive(), role);
    }
}
