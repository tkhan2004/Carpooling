package org.example.carpooling.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String avatarUrl;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
