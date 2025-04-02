package org.example.carpooling.Service;

import org.example.carpooling.Dto.RideRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RideService {
    ResponseEntity<?> createRide(RideRequestDTO rideRequest, String email);
    List<RideRequestDTO> getAllRideActive();
}
