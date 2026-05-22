package com.goldencinema.backend.config;

import com.goldencinema.backend.security.JwtAccessDeniedHandler;
import com.goldencinema.backend.security.JwtAuthenticationEntryPoint;
import com.goldencinema.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2/**")
                        .disable()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/login", "/auth/register", "/auth/test",
                                "/api/auth/login", "/api/auth/register", "/api/auth/test").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/screenings").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/screenings").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/movies/*/screenings").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/movies/*/screenings").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/movies", "/movies/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/movies", "/api/movies/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/movies", "/api/movies").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/movies/*", "/api/movies/*").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/movies/*", "/api/movies/*").hasRole("ADMIN")
                        .requestMatchers("/auth/admin").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/halls").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/halls/*").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/halls").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/halls/*").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/halls/*").hasRole("ADMIN")
                        .requestMatchers("/api/admin/screenings", "/api/admin/screenings/*").hasRole("ADMIN")
                        .requestMatchers("/api/admin/users", "/api/admin/users/*").hasRole("ADMIN")
                        .requestMatchers("/api/admin/stats").hasRole("ADMIN")
                        .requestMatchers("/api/admin/reservations", "/api/admin/reservations/*").hasRole("ADMIN")
                        .requestMatchers("/api/admin/reports", "/api/admin/reports/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reservations").hasRole("USER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reservations/my").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}