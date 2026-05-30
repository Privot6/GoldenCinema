package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateUserRequest;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.UpdateUserRequest;
import com.goldencinema.backend.dto.UserSummaryDto;
import com.goldencinema.backend.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler panelu admina do zarządzania kontami użytkowników.
 * Umożliwia przeglądanie, tworzenie, edycję i usuwanie użytkowników.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Zwraca stronicowaną listę wszystkich użytkowników.
     *
     * @param page numer strony (od 0)
     * @param size liczba elementów na stronie (domyślnie 20)
     * @return strona użytkowników z metadanymi paginacji
     */
    @GetMapping
    public PagedResponse<UserSummaryDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminUserService.getAllUsersPaged(page, size);
    }

    /**
     * Tworzy nowe konto użytkownika lub pracownika.
     *
     * @param request dane nowego użytkownika (imię, nazwisko, email, hasło, rola)
     * @return utworzony użytkownik
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummaryDto createUser(@RequestBody CreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    /**
     * Aktualizuje dane konta użytkownika.
     *
     * @param id      identyfikator użytkownika
     * @param request nowe dane użytkownika
     * @return zaktualizowany użytkownik
     */
    @PutMapping("/{id}")
    public UserSummaryDto updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return adminUserService.updateUser(id, request);
    }

    /**
     * Usuwa konto użytkownika. Administrator nie może usunąć własnego konta.
     *
     * @param id             identyfikator użytkownika do usunięcia
     * @param authentication kontekst zalogowanego administratora
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        adminUserService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
