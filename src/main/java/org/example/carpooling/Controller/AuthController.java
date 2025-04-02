package org.example.carpooling.Controller;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.carpooling.Dto.LoginRequest;
import org.example.carpooling.Dto.LoginResponse;
import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.Imp.UserServiceImp;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping( "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        userService.register(request);
        return ResponseEntity.ok("Đăng ký thành công");
    }// Đăng ký

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. Xác thực email + password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 2. Lấy user từ DB
            Optional<Users> optionalUser = userRepository.findByEmail(request.getEmail());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(404).body("Người dùng không tồn tại");
            }

            Users user = optionalUser.get();

            // 3. Sinh token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
            // 4. Trả response
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getRole().getName() // hoặc user.getRole().getRoleName() tùy bạn đặt tên
            ));

        }

        catch (Exception e) {


//            byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
//            String base64Key = Base64.getEncoder().encodeToString(key);
//            System.out.println("JWT Secret Key (Base64): " + base64Key);
//            => xin key
            return ResponseEntity.status(401).body("Sai tài khoản hoặc mật khẩu");
        }
    }


}
