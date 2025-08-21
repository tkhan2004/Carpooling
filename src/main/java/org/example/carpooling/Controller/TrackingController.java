package org.example.carpooling.Controller;

import org.example.carpooling.Dto.TrackingPayloadDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.RedisService.RedisTrackingPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import javax.sound.midi.Track;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class TrackingController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTrackingPublisher redisTrackingPublisher;

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

}
