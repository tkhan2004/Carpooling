package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.ChangePassDTO;
import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Dto.UserUpdateDTO;
import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.RoleRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

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
    public Users register(RegisterRequest request) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Lấy role từ DB
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
        user.setRole(role);

        userRepository.save(user);
        return user;
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

        // Upload image if present
        // Upload image if present
        if (userUpdateDTO.getAvatarImage() != null) {
            saveImage(token, userUpdateDTO.getAvatarImage(), "avatar");
        }
        if (userUpdateDTO.getLicenseImageUrl() != null) {
            saveImage(token, userUpdateDTO.getLicenseImageUrl(), "license");
        }
        if (userUpdateDTO.getVehicleImageUrl() != null) {
            saveImage(token, userUpdateDTO.getVehicleImageUrl(), "vehicle");
        }

        userRepository.save(user);
        return "Cập nhật thành công";
    }

    @Override
    public String saveImage(String token, MultipartFile file, String type) {
        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "Người dùng không tồn tại";
        }

        Users user = optionalUser.get();
        String uploadPath = "uploads/";
        new File(uploadPath).mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadPath + fileName;

        try {
            Files.copy(file.getInputStream(), new File(filePath).toPath());
            switch (type) {
                case "avatar" -> user.setAvatarImage(filePath);
                case "license" -> user.setLicenseImageUrl(filePath);
                case "vehicle" -> user.setVehicleImageUrl(filePath);
            }
            userRepository.save(user);
            return "Upload thành công";
        } catch (IOException e) {
            return "Lỗi khi upload ảnh";
        }
    }

}

