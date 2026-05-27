package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CreateUserRequest;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.UpdateUserRequest;
import com.goldencinema.backend.dto.UserSummaryDto;
import com.goldencinema.backend.entity.Role;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.repository.RoleRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public PagedResponse<UserSummaryDto> getAllUsersPaged(int page, int size) {
        Page<User> result = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public UserSummaryDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRoles(Set.of(findRoleByName(request.getRole())));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toDto(userRepository.save(user));
    }

    public UserSummaryDto createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Użytkownik z tym emailem już istnieje");
        }
        Role role = findRoleByName(request.role());
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsActive(true);
        user.setRoles(Set.of(role));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return toDto(userRepository.save(user));
    }

    public void deleteUser(Long id, String currentUserEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nie można usunąć własnego konta");
        }
        userRepository.delete(user);
    }

    private Role findRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieznana rola: " + name));
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
