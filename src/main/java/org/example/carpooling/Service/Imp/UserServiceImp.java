package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.*;
import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Exception.Exception;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.RoleRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public Users passengerRegister(RegisterRequest request,
                                   MultipartFile avatarImage) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(null);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception(request.getEmail());
        }

        if (avatarImage != null && !avatarImage.isEmpty()) {
            user.setAvatarImage(saveImage(avatarImage, "avatar", user));
        }
        // Lấy role từ DB
        Role driverRole = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Driver role not found"));
        user.setRole(driverRole);


        userRepository.save(user);
        return user;
    }

    @Override
    public Users driverRegister(RegisterRequest request,
                                MultipartFile avatarImage,
                                MultipartFile licenseImage,
                                MultipartFile vehicleImage) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception(request.getEmail());
        }
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Handle file uploads
        if (avatarImage != null && !avatarImage.isEmpty()) {
            user.setAvatarImage(saveImage(avatarImage, "avatar", user));
        }
        if (licenseImage != null && !licenseImage.isEmpty()) {
            user.setLicenseImageUrl(saveImage(licenseImage, "License", user));
        }
        if (vehicleImage != null && !vehicleImage.isEmpty()) {
            user.setVehicleImageUrl(saveImage(vehicleImage, "Vehicle", user));
        }

        // Set driver role
        Role driverRole = roleRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Driver role not found"));
        user.setRole(driverRole);

        return userRepository.save(user);
    }

    @Override
    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String changePass(String token, ChangePassDTO changePassDTO) {
        String email = jwtUtil.extractUsername(token);
        Optional<Users>  optionalUsers = userRepository.findByEmail(email);

        if(!optionalUsers.isPresent()){
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        Users users =  optionalUsers.get();
        if(passwordEncoder.matches(changePassDTO.getOldPass(), users.getPassword())){
            users.setPassword(passwordEncoder.encode(changePassDTO.getNewPass()));
            userRepository.save(users);
        }else{
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        return " Đổi mật khẩu thành công ";
    }

    // UserServiceImp.java
    @Override
    public String updateProfile(String token, UserUpdateDTO userUpdateDTO) {
        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "Người dùng không tồn tại";
        }

        Users user = optionalUser.get();
        user.setFullName(userUpdateDTO.getFullName());
        user.setPhone(userUpdateDTO.getPhone());

        // ✅ Update avatar cho mọi user
        if (userUpdateDTO.getAvatarImage() != null) {
            saveImage(userUpdateDTO.getAvatarImage(), "avatar", user);
        }

        // ✅ Chỉ cho tài xế mới được upload ảnh license & vehicle
        if (user.getRole().getName().equalsIgnoreCase("DRIVER")) {
            if (userUpdateDTO.getLicenseImageUrl() != null) {
                saveImage(userUpdateDTO.getLicenseImageUrl(), "license", user);
            }
            if (userUpdateDTO.getVehicleImageUrl() != null) {
                saveImage(userUpdateDTO.getVehicleImageUrl(), "vehicle", user);
            }
        }

        userRepository.save(user);
        return "Cập nhật thành công";
    }

    @Override
    public List<?> getUsersByRole(String role) {
        List<Users> users = userRepository.findAllByRole(role);
        return users.stream()
                .map(user -> {
                    switch (role.toUpperCase()) {
                        case "DRIVER":
                            return new DriverDTO(
                                    user.getId(),
                                    user.getFullName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    user.getRole().getName(),
                                    user.getStatus()
                            );
                        case "PASSENGER":
                            return new UserDTO(
                                    user.getId(),
                                    user.getFullName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    user.getRole().getName()
                            );
                        default:
                            throw new IllegalArgumentException("Unknown role: " + role);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean rejectUser(Long id,String rejectionReason) {
        Optional<Users> optionalUser = userRepository.findUsersById(id);
        if (optionalUser.isEmpty()) {return false;}
        Users user = optionalUser.get();
        user.setStatus(DriverStatus.REJECTED);
        user.setRejectionReason(rejectionReason);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean approvedUser(Long id) {
        Optional<Users> optionalUser = userRepository.findUsersById(id);
        if (optionalUser.isEmpty()) {return false;}
        Users user = optionalUser.get();
        user.setStatus(DriverStatus.APPROVED);
        user.setRejectionReason(null);
        userRepository.save(user);
        return true;
    }

    public String saveImage(MultipartFile file, String type, Users user) {
        if (file == null || file.isEmpty()) return null;

        String uploadPath = "uploads/";
        new File(uploadPath).mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadPath + fileName;

        try {
            Files.copy(file.getInputStream(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            switch (type) {
                case "avatar" -> user.setAvatarImage(filePath);
                case "license" -> user.setLicenseImageUrl(filePath);
                case "vehicle" -> user.setVehicleImageUrl(filePath);
            }
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

