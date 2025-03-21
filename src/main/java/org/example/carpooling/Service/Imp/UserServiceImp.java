package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.RegisterRequest;
import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.RoleRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepository;

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
}
