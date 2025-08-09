package org.example.carpooling.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse
{
    private String token;
    private String email;
    private String role;

}
