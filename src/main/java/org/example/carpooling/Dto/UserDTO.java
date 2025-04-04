package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Users;

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

    public UserDTO(Long id, String fullName, String email, String phoneNumber, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    public UserDTO(Users user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhone();
        if (user.getRole() != null) {
            this.role = user.getRole().getName();
        } else {
            this.role = "";
        }
    }

    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
