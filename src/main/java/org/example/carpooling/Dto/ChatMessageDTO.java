package org.example.carpooling.Dto;
import lombok.*;
import org.example.carpooling.Entity.ChatMessage;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {
    private String token;           // token từ client
    private String senderEmail;     // email người gửi
    private String receiverEmail;   // email người nhận

    public ChatMessageDTO(ChatMessage message  ) {
        this.token = token;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.senderName = senderName;
        this.content = content;
        this.roomId = roomId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    private String senderName;      // tên người gửi
    private String content;         // nội dung tin nhắn
    private String roomId;          // ID phòng chat
    private LocalDateTime timestamp; // thời gian gửi
    private boolean isRead = false;    // trạng thái đã đọc
}
