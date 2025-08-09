package org.example.carpooling.Dto;

import lombok.*;
import org.example.carpooling.Entity.Notification;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String userEmail;
    private String title;
    private String content;
    private String type;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
    

} 