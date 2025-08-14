package org.example.carpooling.Service;

import org.example.carpooling.Dto.*;
import org.example.carpooling.Dto.Request.ChangePassDTO;
import org.example.carpooling.Dto.Request.UserUpdateRequestDTO;
import org.example.carpooling.Entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Users passengerRegister(String email, String phone, String password, String fullName, MultipartFile avatarImage);
    Users driverRegister(String email, String phone, String password, String fullName,
                         String licensePlate, String brand, String model, String color,Integer numberOfSeats,
                         MultipartFile avatarImage,
                         MultipartFile licenseImage,
                         MultipartFile vehicleImage);
    Optional<Users> findByEmail(String email);
    String changePass(String token, ChangePassDTO changePassDTO);
    String updateProfile(String token, UserUpdateRequestDTO userUpdateDTO) throws IOException;
    List<?> getUsersByRole(String roleName);
    boolean rejectUser(Long id,String rejectionReason);
    boolean approvedUser(Long id);
    DriverDTO getUserDetails(Long id);
    Boolean deleteUser(Long id);
}