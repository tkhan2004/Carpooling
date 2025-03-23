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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        return Optional.empty();
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

    @Override
    public String updateProfile(String token, UserUpdateDTO userUpdateDTO) {
        String email = jwtUtil.extractUsername(token);
        Optional<Users>  optionalUsers = userRepository.findByEmail(email);
        if(!optionalUsers.isPresent()){
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        Users users =  optionalUsers.get();
        users.setFullName(userUpdateDTO.getFullName());
        users.setPhone(userUpdateDTO.getPhone());

        if(users.getRole().getName().equals("DRIVER")){
            users.setAvatarImage(userUpdateDTO.getAvatarImage());
            users.setLicenseImageUrl(userUpdateDTO.getLicenseImageUrl());
            users.setVehicleImageUrl(userUpdateDTO.getVehicleImageUrl());
        }
        userRepository.save(users);
        return " Cập nhật thông tin thành công";
    }


}
