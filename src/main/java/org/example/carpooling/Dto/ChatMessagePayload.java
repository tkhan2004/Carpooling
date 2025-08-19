package org.example.carpooling.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessagePayload {
    private String roomId;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
}
