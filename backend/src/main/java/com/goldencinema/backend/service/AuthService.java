package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.LoginRequest;
import com.goldencinema.backend.dto.LoginResponse;
import com.goldencinema.backend.dto.RegisterRequest;
import com.goldencinema.backend.dto.RegisterResponse;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.exception.InvalidCredentialsException;
import com.goldencinema.backend.exception.UserAlreadyExistsException;
import com.goldencinema.backend.repository.UserRepository;
import com.goldencinema.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik z takim emailem już istnieje");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_USER");

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Nieprawidłowy email lub hasło"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return new LoginResponse(token, "Bearer");
    }
}