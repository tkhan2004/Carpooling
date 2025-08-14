package org.example.carpooling.Dto.Request;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassDTO {
    private  String oldPass;
    private String newPass;

}
