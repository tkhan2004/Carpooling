package org.example.carpooling.Service;

import org.example.carpooling.Dto.ChangePassDTO;
import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Dto.UserUpdateDTO;
import org.example.carpooling.Entity.Users;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {
    Users register(RegisterRequest registerRequest);
    Optional<Users> findByEmail(String email);
    String changePass(String token, ChangePassDTO  changePassDTO);
    String updateProfile(String token, UserUpdateDTO userUpdateDTO);
}
