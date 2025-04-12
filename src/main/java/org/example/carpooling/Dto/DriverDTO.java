package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Users;

import java.util.stream.Collectors;

public class DriverDTO {
    private Long id;

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    private DriverStatus status;
    private String licenseImageUrl;
    private String vehicleImageUrl;
    private String avatarImage;



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

    public String getLicenseImageUrl() {
        return licenseImageUrl;
    }

    public void setLicenseImageUrl(String licenseImageUrl) {
        this.licenseImageUrl = licenseImageUrl;
    }

    public String getVehicleImageUrl() {
        return vehicleImageUrl;
    }

    public void setVehicleImageUrl(String vehicleImageUrl) {
        this.vehicleImageUrl = vehicleImageUrl;
    }

    public String getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(String avatarImage) {
        this.avatarImage = avatarImage;
    }

    public DriverDTO(Long id, DriverStatus status, String licenseImageUrl, String vehicleImageUrl, String avatarImage, String fullName, String email, String phoneNumber, String role) {
        this.id = id;
        this.status = status;
        this.licenseImageUrl = licenseImageUrl;
        this.vehicleImageUrl = vehicleImageUrl;
        this.avatarImage = avatarImage;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public DriverDTO(Long id, String fullName, String email, String phoneNumber, String role,DriverStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;

    }

    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
