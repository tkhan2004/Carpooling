package org.example.carpooling.Repository;
import org.example.carpooling.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Lấy tin nhắn theo phòng
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // Lấy tin nhắn giữa người gửi và người nhận
    List<ChatMessage> findBySenderEmailAndReceiverEmailOrderByTimestampAsc(String senderEmail, String receiverEmail);

    // Đánh dấu tin nhắn đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage c SET c.isRead = true WHERE c.roomId = :roomId AND c.receiverEmail = :userEmail AND c.isRead = false")
    void markMessagesAsRead(@Param("roomId") String roomId, @Param("userEmail") String userEmail);

    // Đếm tin nhắn chưa đọc
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.roomId = :roomId AND c.receiverEmail = :userEmail AND c.isRead = false")
    long countUnreadMessages(@Param("roomId") String roomId, @Param("userEmail") String userEmail);

    // Lấy danh sách phòng chat của một người dùng
    @Query("SELECT DISTINCT c.roomId FROM ChatMessage c WHERE c.senderEmail = :userEmail OR c.receiverEmail = :userEmail")
    List<String> findRoomIdsByUserEmail(@Param("userEmail") String userEmail);

    // Lấy tin nhắn mới nhất trong một phòng chat
    @Query("SELECT c FROM ChatMessage c WHERE c.roomId = :roomId ORDER BY c.timestamp DESC")
    List<ChatMessage> findLatestMessagesByRoomId(@Param("roomId") String roomId);

    // Helper method to get the single latest message
    default ChatMessage findLatestMessageByRoomId(@Param("roomId") String roomId) {
        List<ChatMessage> messages = findLatestMessagesByRoomId(roomId);
        return messages.isEmpty() ? null : messages.get(0);
    }
}