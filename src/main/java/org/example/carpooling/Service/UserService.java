package org.example.carpooling.Service;

import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Entity.Users;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {
    Users register(RegisterRequest registerRequest);
    Optional<Users> findByEmail(String email);
}
