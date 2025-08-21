package org.example.carpooling.Service.RedisService;

import org.example.carpooling.Dto.TrackingPayloadDTO;

public interface RedisTrackingPublisher {
    void publish(TrackingPayloadDTO tracking);
}
