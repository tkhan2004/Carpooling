package org.example.carpooling.Dto;

import org.springframework.web.multipart.MultipartFile;

public class RegisterRequest {
    private String email;
    private Long roleId; // Thêm dòng này
    private String phone;


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }


//    public RegisterRequest(String email, String password, String fullName) {
//        this.email = email;
//        this.password = password;
//        this.fullName = fullName;
//    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private String password;
    private String fullName;
}
