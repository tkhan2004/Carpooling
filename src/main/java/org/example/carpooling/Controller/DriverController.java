package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String username = jwtUtil.extractUsername(token);
        Optional<Users> users =  userRepository.findByEmail(username);
//        System.out.println("Extracted username from token: " + username);
//        System.out.println("Tìm thấy user: " + users.isPresent());
        if (!users.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }

        Users user = users.get();
        UserDTO userDTO = new UserDTO(user.getId(),user.getFullName(),user.getEmail(),user.getPhone(),user.getRole().getName());
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

}
