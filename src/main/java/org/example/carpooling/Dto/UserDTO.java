package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Service.FileService;

import java.util.stream.Collectors;

public class UserDTO {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserDTO(Long id, String fullName, String email, String phoneNumber, String role,String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }
    public UserDTO(Users user, FileService fileService) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhone();
        this.role = user.getRole() != null ? user.getRole().getName() : "";

        if (user.getAvatarImage() != null && !user.getAvatarImage().trim().isEmpty()) {
            this.avatarUrl = fileService.generateFileUrl(user.getAvatarImage());
        } else {
            this.avatarUrl = "/default-avatar.png"; // hoặc null, tùy bạn xử lý frontend
        }
    }

    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
