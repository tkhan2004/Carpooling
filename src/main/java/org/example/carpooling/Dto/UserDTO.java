package org.example.carpooling.Dto;

import lombok.*;
import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Service.FileService;

import java.util.stream.Collectors;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String avatarUrl;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
