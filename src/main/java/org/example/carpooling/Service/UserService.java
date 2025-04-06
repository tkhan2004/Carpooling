package org.example.carpooling.Service;

import org.example.carpooling.Dto.*;
import org.example.carpooling.Entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Users passengerRegister(RegisterRequest registerRequest,MultipartFile avatarImage);
    Users driverRegister(RegisterRequest registerRequest, MultipartFile avatarImage,
                         MultipartFile licenseImage,
                         MultipartFile vehicleImage);
    Optional<Users> findByEmail(String email);
    String changePass(String token, ChangePassDTO  changePassDTO);
    String saveImage(MultipartFile file, String type, Users user);
    String updateProfile(String token, UserUpdateDTO userUpdateDTO);
    List<?> getUsersByRole(String roleName);
    boolean rejectUser(Long id,String rejectionReason);
    boolean approvedUser(Long id);
}