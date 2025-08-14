package org.example.carpooling.Dto.Request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email;

    private String password;

}
