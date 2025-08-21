package org.example.carpooling.Service.Imp.RedisServiceImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carpooling.Dto.TrackingPayloadDTO;
import org.example.carpooling.Service.RedisService.RedisTrackingSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTrackingSubscriberImp implements RedisTrackingSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisTrackingSubscriberImp(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }


    @Override
    public void onMessage(String message, String channel)  {
        try {
            TrackingPayloadDTO location = objectMapper.readValue(message, TrackingPayloadDTO.class);
            String destination = "/topic/tracking" + location.getRideId();
            messagingTemplate.convertAndSend(destination, location);
            System.out.println("Redis nhận vị trí: " + location.getLatitude() + "," + location.getLongitude() +
                    " ride=" + location.getRideId());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
