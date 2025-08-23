package org.example.carpooling.Helper;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyBase64;
    private long timeFreshesToken = 3600000L * 24 * 30;
    private final long EXPIRATION_TIME =timeFreshesToken ; // 1 tiếng

    private byte[] getSecretKey() {
        return Base64.getDecoder().decode(secretKeyBase64);
    }

    // CÁI NÀY LÀ MỖI LẦN ĐĂNG NHẬP NÓ CHO 1 TOKEN RIÊNG
    // JWT KHÔNG LƯU TOKEN TRÊN SERVER
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) // PHẦN USERNAME TRẢ VỀ TOKEN
                .claim("role", role) // Thêm role vào token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKeyBase64)
                .compact();
    }

    // CÁI NÀY TRẢ VỀ TOKEN MỖI USERNAME
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // CÁI NÀY THÌ THEO ROLE
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKeyBase64)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    //
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Cắt bỏ "Bearer "
        }
        throw new RuntimeException("Missing token");
    }
}