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

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Serwis administracyjny do zarządzania kontami użytkowników.
 * Umożliwia tworzenie, edycję i usuwanie użytkowników przez administratora.
 */
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

    /**
     * Zwraca stronicowaną listę użytkowników, posortowanych malejąco po dacie rejestracji.
     *
     * @param page numer strony (od 0)
     * @param size liczba elementów na stronie
     * @return strona użytkowników z metadanymi paginacji
     */
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

    /**
     * Aktualizuje dane konta użytkownika. Nie można edytować konta administratora.
     *
     * @param id      identyfikator użytkownika
     * @param request nowe dane użytkownika (pola opcjonalne — aktualizowane tylko podane)
     * @return zaktualizowany użytkownik
     * @throws ResponseStatusException (403) gdy próba edycji konta administratora
     * @throws ResponseStatusException (409) gdy nowy email jest już zajęty
     */
    @Transactional
    public UserSummaryDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        if (isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie można edytować konta administratora");
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role newRole = findRoleByName(request.getRole());
            user.getRoles().clear();
            user.getRoles().add(newRole);
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                    userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Użytkownik z tym emailem już istnieje");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toDto(userRepository.save(user));
    }

    /**
     * Tworzy nowe konto użytkownika z podaną rolą.
     *
     * @param request dane nowego użytkownika (imię, nazwisko, email, hasło, rola)
     * @return utworzony użytkownik
     * @throws ResponseStatusException (409) gdy email jest już zajęty
     */
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

    /**
     * Usuwa konto użytkownika. Administrator nie może usunąć własnego konta.
     *
     * @param id               identyfikator użytkownika do usunięcia
     * @param currentUserEmail email zalogowanego administratora
     * @throws ResponseStatusException (409) gdy próba usunięcia własnego konta
     */
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
