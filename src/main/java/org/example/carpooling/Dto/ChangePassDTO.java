package org.example.carpooling.Dto;

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
