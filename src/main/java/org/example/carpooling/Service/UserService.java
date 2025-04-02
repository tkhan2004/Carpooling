package org.example.carpooling.Service;

import org.example.carpooling.Dto.ChangePassDTO;
import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Dto.UserUpdateDTO;
import org.example.carpooling.Entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Users register(RegisterRequest registerRequest);
    Optional<Users> findByEmail(String email);
    String changePass(String token, ChangePassDTO  changePassDTO);
    String saveImage(String token, MultipartFile file, String type);
    String updateProfile(String token, UserUpdateDTO userUpdateDTO);
}
