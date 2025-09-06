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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <-- ADD THIS LINE FOR CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permit OPTIONS preflight requests globally
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() // Assuming /api/auth includes /login
                        .requestMatchers("/api/file/**").permitAll() // 👈 Thêm dòng này// Your existing public and role-based rules
                        .requestMatchers("/api/file/view/**").permitAll() // 👈 Thêm dòng này// Your existing public and role-based rules
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // This protects /api/admin/user
                        .requestMatchers("/api/driver/**").hasRole("DRIVER")
                        .requestMatchers("/api/passenger/**").hasRole("PASSENGER")
                        .requestMatchers("/ws/**").permitAll()
                        // Swagger UI and OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
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
        configuration.setAllowedOrigins(Arrays.asList(
                "http://127.0.0.1:5500",
                "http://127.0.0.1:5502",
                "http://localhost:5500",
                "http://localhost:55668",
                "http://localhost:63342",
                "https://carpooling-j5xn.onrender.com",
                "https://carpooling-admin-nine.vercel.app"
        ));
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*.*:*",  // Cho mobile testing
                "http://10.*.*.*:*"      // Cho mobile testing
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // cho an toàn
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true); // ok vì bạn đã chỉ rõ origin

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
