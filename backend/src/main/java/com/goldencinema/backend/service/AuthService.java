package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.LoginRequest;
import com.goldencinema.backend.dto.LoginResponse;
import com.goldencinema.backend.dto.RegisterRequest;
import com.goldencinema.backend.entity.Role;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.exception.InvalidCredentialsException;
import com.goldencinema.backend.exception.UserAlreadyExistsException;
import com.goldencinema.backend.repository.RoleRepository;
import com.goldencinema.backend.repository.UserRepository;
import com.goldencinema.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Serwis obsługujący rejestrację i logowanie użytkowników.
 * Generuje tokeny JWT po pomyślnym uwierzytelnieniu.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Rejestruje nowego użytkownika z rolą USER i zwraca token JWT.
     *
     * @param request dane rejestracyjne
     * @return token JWT i typ tokenu
     * @throws UserAlreadyExistsException gdy email jest już zajęty
     */
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Ten adres email jest już zajęty");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rola USER nie istnieje"));

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        String token = jwtService.generateToken(request.email(), userRole.getName());
        return new LoginResponse(token, "Bearer");
    }

    /**
     * Uwierzytelnia użytkownika i zwraca token JWT.
     *
     * @param request dane logowania (email, hasło)
     * @return token JWT i typ tokenu
     * @throws InvalidCredentialsException gdy email lub hasło jest nieprawidłowe, lub konto jest nieaktywne
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Nieprawidłowy email lub hasło"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("Konto jest nieaktywne");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło");
        }

        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElseThrow(() -> new InvalidCredentialsException("Użytkownik nie ma przypisanej roli"));

        String token = jwtService.generateToken(user.getEmail(), role);

        return new LoginResponse(token, "Bearer");
    }
}