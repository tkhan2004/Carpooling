package org.example.carpooling.Dto;


public class LoginRequest {
    private String email;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
