package com.nckh.yte.config;

import com.nckh.yte.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ⚙️ Cấu hình bảo mật chính của hệ thống (JWT + Roles)
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // ✅ Preflight (browser)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ Public: login + swagger
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // ❌ KHÔNG còn permitAll cho /api/ai/**
                .requestMatchers("/api/ai/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "NURSE", "PATIENT")

                // ✅ Admin APIs
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ✅ Doctor & Nurse APIs
                .requestMatchers("/api/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/api/nurse/**").hasAnyRole("NURSE", "ADMIN")

                // ✅ Patient APIs
                .requestMatchers(HttpMethod.GET, "/api/patients/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                .requestMatchers(HttpMethod.POST, "/api/patients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")

                // ✅ Appointment APIs
                .requestMatchers(HttpMethod.GET, "/api/appointments/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "NURSE", "PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/appointments/auto-schedule")
                    .hasAnyRole("PATIENT", "DOCTOR")
                .requestMatchers("/api/appointments/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "NURSE")

                // ✅ Info APIs
                .requestMatchers("/api/info/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "NURSE", "PATIENT")

                // ✅ Mặc định: cần xác thực
                .anyRequest().authenticated()
            )

            // ⚙️ Stateless JWT
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ⚙️ Thêm filter JWT vào chain
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("✅ SecurityConfig loaded (roles: ADMIN, DOCTOR, NURSE, PATIENT)");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
