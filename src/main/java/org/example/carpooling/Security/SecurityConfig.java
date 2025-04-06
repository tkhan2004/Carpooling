package org.example.carpooling.Security;

import org.example.carpooling.Service.Imp.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- Import HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Import EnableMethodSecurity if you use @PreAuthorize, @Secured, etc.
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // <-- Import CORS config
import org.springframework.web.cors.CorsConfigurationSource; // <-- Import CORS source
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- Import CORS source impl

import java.util.Arrays; // <-- Import Arrays
import java.util.List; // <-- Import List (if needed, Arrays.asList is fine)

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- ADD THIS ANNOTATION - Required for @PreAuthorize to work
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter; // Renamed variable for clarity

    @Autowired
    PasswordEncoder passwordEncoder;

    // Constructor injection is good practice
    public SecurityConfig(CustomUserDetailService customUserDetailService) {
        this.customUserDetailService = customUserDetailService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS using the CorsConfigurationSource bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <-- ADD THIS LINE FOR CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permit OPTIONS preflight requests globally
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // <-- ADD THIS LINE
                        // Your existing public and role-based rules
                        .requestMatchers("/api/auth/**").permitAll() // Assuming /api/auth includes /login
                        .requestMatchers("/api/driver/**").hasRole("DRIVER")
                        .requestMatchers("/api/passenger/**").hasRole("PASSENGER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // This protects /api/admin/user
                        // Add other permitAll endpoints if needed (e.g., swagger-ui)
                        // .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated() // All other requests need authentication
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Define the CORS configuration source bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // IMPORTANT: Specify the exact origin of your frontend
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500", "http://localhost:5500")); // Allow VScode Live Server
        // Specify allowed methods (including OPTIONS for preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Specify allowed headers (Authorization is crucial for JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        // Allow credentials (needed for Authorization header)
        configuration.setAllowCredentials(true);
        // You can also expose headers if your frontend needs to read them
        // configuration.setExposedHeaders(Arrays.asList("Custom-Header1"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all paths - you can restrict it if needed e.g., "/api/**"
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}