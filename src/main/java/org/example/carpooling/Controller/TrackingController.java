package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.carpooling.Dto.TrackingPayloadDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.RedisService.RedisTrackingPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Track;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("api/")
@Tag(name = "Tracking", description = "API theo dõi vị trí người dùng")
public class TrackingController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTrackingPublisher redisTrackingPublisher;

    @Operation(summary = "Xử lý tracking",
            description = "Xử lý tracking gửi qua WebSocket, lưu vào cơ sở dữ liệu và phát hành qua Redis")

    @MessageMapping("/tracking/{rideId}")
    public void processLocation(@DestinationVariable String rideId, TrackingPayloadDTO trackingPayloadDTO){
        try{
        String email = jwtUtil.extractUsername(trackingPayloadDTO.getDriverEmail());
        Optional<Users> driver = userRepository.findByEmail(email);

        if (driver.isEmpty()) return;
            trackingPayloadDTO.setDriverEmail(email);
            trackingPayloadDTO.setRideId(rideId);
            trackingPayloadDTO.setTimestamp(LocalDateTime.now());

            redisTrackingPublisher.publish(trackingPayloadDTO);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Operation(summary = "Test gửi vị trí driver",
            description = "Dùng để Swagger test, phát hành vị trí qua Redis")
    @PostMapping("/tracking/test/{rideId}")
    public ApiResponse<TrackingPayloadDTO> testTracking(
            @PathVariable String rideId,
            @RequestBody TrackingPayloadDTO trackingPayloadDTO,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // check token
            String email = jwtUtil.extractUsername(trackingPayloadDTO.getDriverEmail());
            Optional<Users> driver = userRepository.findByEmail(email);

            if (driver.isEmpty()) {
                return new ApiResponse<>(false, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST.value(), null);
            }

            // set thông tin vị trí
            trackingPayloadDTO.setDriverEmail(email);
            trackingPayloadDTO.setRideId(rideId);
            trackingPayloadDTO.setTimestamp(LocalDateTime.now());

            // publish qua Redis
            redisTrackingPublisher.publish(trackingPayloadDTO);

            return new ApiResponse<>(true, "Gửi vị trí thành công", HttpStatus.OK.value(), trackingPayloadDTO);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Lỗi khi gửi vị trí", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
    }

}
