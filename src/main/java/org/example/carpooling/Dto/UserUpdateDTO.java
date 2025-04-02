package org.example.carpooling.Dto;

import org.springframework.web.multipart.MultipartFile;

public class UserUpdateDTO {
    private String fullName;
    private String phone;
    private MultipartFile avatarImage;
    private MultipartFile licenseImageUrl;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MultipartFile getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(MultipartFile avatarImage) {
        this.avatarImage = avatarImage;
    }

    public MultipartFile getLicenseImageUrl() {
        return licenseImageUrl;
    }

    public void setLicenseImageUrl(MultipartFile licenseImageUrl) {
        this.licenseImageUrl = licenseImageUrl;
    }

    public MultipartFile getVehicleImageUrl() {
        return vehicleImageUrl;
    }

    public void setVehicleImageUrl(MultipartFile vehicleImageUrl) {
        this.vehicleImageUrl = vehicleImageUrl;
    }

    private MultipartFile vehicleImageUrl;

    // Getter và Setter
}