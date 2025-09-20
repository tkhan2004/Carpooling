package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.*;
import org.example.carpooling.Dto.Request.ChangePassDTO;
import org.example.carpooling.Dto.Request.UserUpdateRequestDTO;
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

import java.io.IOException;
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
    public Users passengerRegister(String email, String phone, String password, String fullName,
                                   MultipartFile avatarImage) {
        Users user = new Users();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
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
        Role passengerRole = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Driver role not found"));
        user.setRole(passengerRole);


        userRepository.save(user);
        return user;
    }

    @Override
    public Users driverRegister(String email, String phone, String password, String fullName,
                                String licensePlate, String brand, String model, String color, Integer numberOfSeats,
                                MultipartFile avatarImage,
                                MultipartFile licenseImage,
                                MultipartFile vehicleImage) {

        Users user = new Users();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã tồn tại");
        }

        // Upload avatar
        if (avatarImage != null && !avatarImage.isEmpty()) {
            try {
                Map<String, String> avatarData = cloudinaryService.upLoadFile(avatarImage);
                user.setAvatarImage(avatarData.get("url"));
                user.setAvatarImagePublicId(avatarData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Tạo vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(brand);
        vehicle.setColor(color);
        vehicle.setModel(model);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setNumberOfSeats(numberOfSeats);

        // Upload license image
        if (licenseImage != null && !licenseImage.isEmpty()) {
            try {
                Map<String, String> licenseData = cloudinaryService.upLoadFile(licenseImage);
                vehicle.setLicenseImageUrl(licenseData.get("url")); // ✅ set vào vehicle
                vehicle.setLicenseImagePublicId(licenseData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Upload vehicle image
        if (vehicleImage != null && !vehicleImage.isEmpty()) {
            try {
                Map<String, String> vehicleData = cloudinaryService.upLoadFile(vehicleImage);
                vehicle.setVehicleImageUrl(vehicleData.get("url")); // ✅ set vào vehicle
                vehicle.setVehicleImagePublicId(vehicleData.get("publicId"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Set role
        Role driverRole = roleRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Driver role not found"));
        user.setRole(driverRole);

        // Gán quan hệ
        vehicle.setDriver(user);
        user.setVehicles(Collections.singletonList(vehicle));

        return userRepository.save(user); // Hibernate cascade sẽ lưu cả vehicle
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
    public String updateProfile(
            String token,
            UserUpdateRequestDTO userUpdateDTO,
            MultipartFile avatarFile,
            MultipartFile licenseFile,
            MultipartFile vehicleFile) throws IOException {

        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "Người dùng không tồn tại";
        }

        Users user = optionalUser.get();

        // Update text fields
        if (userUpdateDTO.getFullName() != null) {
            user.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getPhone() != null) {
            user.setPhone(userUpdateDTO.getPhone());
        }

        // ✅ Update avatar cho mọi user
        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (user.getAvatarImagePublicId() != null) {
                cloudinaryService.deleteFile(user.getAvatarImagePublicId());
            }
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
            if (licenseFile != null && !licenseFile.isEmpty()) {
                if (vehicle.getLicenseImagePublicId() != null) {
                    cloudinaryService.deleteFile(vehicle.getLicenseImagePublicId());
                }
                Map<String, String> licenseData = cloudinaryService.upLoadFile(licenseFile);
                vehicle.setLicenseImageUrl(licenseData.get("url"));
                vehicle.setLicenseImagePublicId(licenseData.get("publicId"));
                needApproval = true;
            }

            // Update vehicle image
            if (vehicleFile != null && !vehicleFile.isEmpty()) {
                if (vehicle.getVehicleImagePublicId() != null) {
                    cloudinaryService.deleteFile(vehicle.getVehicleImagePublicId());
                }
                Map<String, String> vehicleData = cloudinaryService.upLoadFile(vehicleFile);
                vehicle.setVehicleImageUrl(vehicleData.get("url"));
                vehicle.setVehicleImagePublicId(vehicleData.get("publicId"));
                needApproval = true;
            }

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
