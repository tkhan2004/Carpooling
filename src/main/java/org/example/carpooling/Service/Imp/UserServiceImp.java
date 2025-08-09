package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.*;
import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Entity.Vehicle;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.RoleRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Repository.VehicleRepository;
import org.example.carpooling.Service.CloudinaryService;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private VehicleRepository vehicleRepository;


    @Override
    public Users passengerRegister(RegisterRequest request,
                                   MultipartFile avatarImage) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(null);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã tồn tại");
        }

        if (avatarImage != null && !avatarImage.isEmpty()) {
            try {
                Map<String, String> avatarData = cloudinaryService.upLoadFile(avatarImage);
                user.setAvatarImage(avatarData.get("url"));
                user.setAvatarImagePublicId(avatarData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã tồn tại");

        }

        if (avatarImage != null && !avatarImage.isEmpty()) {
            try {
                Map<String, String> avatarData = cloudinaryService.upLoadFile(avatarImage);
                user.setAvatarImage(avatarData.get("url"));
                user.setAvatarImagePublicId(avatarData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(request.getBrand());
        vehicle.setColor(request.getColor());
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setNumberOfSeats(request.getNumberOfSeats());

        if (licenseImage != null && !licenseImage.isEmpty()) {
            try {
                Map<String, String> LicenseData = cloudinaryService.upLoadFile(licenseImage);
                user.setAvatarImage(LicenseData.get("url"));
                user.setAvatarImagePublicId(LicenseData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (vehicleImage != null && !vehicleImage.isEmpty()) {
            try {
                Map<String, String> vehicleData = cloudinaryService.upLoadFile(vehicleImage);
                user.setAvatarImage(vehicleData.get("url"));
                user.setAvatarImagePublicId(vehicleData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Set driver role
        Role driverRole = roleRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Driver role not found"));
        user.setRole(driverRole);

        // Gán quan hệ
        vehicle.setDriver(user);
        user.setVehicles(Arrays.asList(vehicle));
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
    public String updateProfile(String token, UserUpdateRequestDTO userUpdateDTO) throws IOException {
        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "Người dùng không tồn tại";
        }

        Users user = optionalUser.get();
        user.setFullName(userUpdateDTO.getFullName());
        user.setPhone(userUpdateDTO.getPhone());



        // ✅ Update avatar cho mọi user
        MultipartFile avatarFile = userUpdateDTO.getAvatarImageUrl();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xoá ảnh cũ
            if (user.getAvatarImagePublicId() != null) {
                cloudinaryService.deleteFile(user.getAvatarImagePublicId());
            }

            // Upload ảnh mới
            Map<String, String> avatarData = cloudinaryService.upLoadFile(avatarFile);
            user.setAvatarImage(avatarData.get("url"));
            user.setAvatarImagePublicId(avatarData.get("publicId"));
        }

        // ✅ Nếu là DRIVER thì update license & vehicle
        if ("DRIVER".equalsIgnoreCase(user.getRole().getName())) {
            Vehicle vehicle = vehicleRepository.findByDriverId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin xe"));

            boolean needApproval = false;

            // Update license image
            if (userUpdateDTO.getLicenseImageUrl() != null && !userUpdateDTO.getLicenseImageUrl().isEmpty()) {
                if (vehicle.getLicenseImagePublicId() != null) {
                    cloudinaryService.deleteFile(vehicle.getLicenseImagePublicId());
                }
                Map<String, String> licenseData = cloudinaryService.upLoadFile(userUpdateDTO.getLicenseImageUrl());
                vehicle.setLicenseImageUrl(licenseData.get("url"));
                vehicle.setLicenseImagePublicId(licenseData.get("publicId"));
                needApproval = true;
            }

            // Update vehicle image
            if (userUpdateDTO.getVehicleImageUrl() != null && !userUpdateDTO.getVehicleImageUrl().isEmpty()) {
                if (vehicle.getVehicleImagePublicId() != null) {
                    cloudinaryService.deleteFile(vehicle.getVehicleImagePublicId());
                }
                Map<String, String> vehicleData = cloudinaryService.upLoadFile(userUpdateDTO.getVehicleImageUrl());
                vehicle.setVehicleImageUrl(vehicleData.get("url"));
                vehicle.setVehicleImagePublicId(vehicleData.get("publicId"));
                needApproval = true;
            }

            // Nếu có thay đổi ảnh giấy tờ → set trạng thái PENDING
            if (needApproval) {
                user.setStatus(DriverStatus.PENDING);
            }

            vehicleRepository.save(vehicle);
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
                            Vehicle vehicle = (user.getVehicles() != null && !user.getVehicles().isEmpty())
                                    ? user.getVehicles().get(0) // lấy xe đầu tiên
                                    : null;

                            return DriverDTO.builder()
                                    .id(user.getId())
                                    .status(user.getStatus())
                                    .avatarImage(user.getAvatarImage())
                                    .fullName(user.getFullName())
                                    .email(user.getEmail())
                                    .phoneNumber(user.getPhone())
                                    .role(user.getRole().getName())
                                    .licensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                                    .brand(vehicle != null ? vehicle.getBrand() : null)
                                    .model(vehicle != null ? vehicle.getModel() : null)
                                    .color(vehicle != null ? vehicle.getColor() : null)
                                    .numberOfSeats(vehicle != null ? vehicle.getNumberOfSeats() : null)
                                    .vehicleImageUrl(vehicle != null ? vehicle.getVehicleImageUrl() : null)
                                    .licenseImageUrl(vehicle != null ? vehicle.getLicenseImageUrl() : null)
                                    .build();
                        case "PASSENGER":
                            return new UserDTO(
                                    user.getId(),
                                    user.getAvatarImage(),
                                    user.getAvatarImagePublicId(),
                                    user.getEmail(),
                                    user.getFullName(),
                                    user.getPhone()
                            ); // 👈 constructor mới
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

    @Override
    public DriverDTO getUserDetails(Long id) {
        Optional<Users> optionalUser = userRepository.findUsersById(id);
        if (optionalUser.isEmpty()) {return null;}
        Users user = optionalUser.get();
        Vehicle vehicle = (user.getVehicles() != null && !user.getVehicles().isEmpty())
                ? user.getVehicles().get(0) // lấy xe đầu tiên
                : null;
        return DriverDTO.builder()
                .id(user.getId())
                .status(user.getStatus())
                .avatarImage(user.getAvatarImage())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .role(user.getRole().getName())
                .licensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                .brand(vehicle != null ? vehicle.getBrand() : null)
                .model(vehicle != null ? vehicle.getModel() : null)
                .color(vehicle != null ? vehicle.getColor() : null)
                .numberOfSeats(vehicle != null ? vehicle.getNumberOfSeats() : null)
                .vehicleImageUrl(vehicle != null ? vehicle.getVehicleImageUrl() : null)
                .licenseImageUrl(vehicle != null ? vehicle.getLicenseImageUrl() : null)
                .status(user.getStatus())
                .build();
    }

    @Override
    public Boolean deleteUser(Long id) {
        Optional<Users> optionalUser = userRepository.findUsersById(id);
        if (optionalUser.isEmpty()) {return false;}
        Users user = optionalUser.get();
        userRepository.delete(user);
        return true;
    }

}

