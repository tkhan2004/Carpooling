package org.example.carpooling.Service;

import org.example.carpooling.Dto.RideRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RideService {
    public void createRide(RideRequestDTO rideRequest, String email) ;
    List<RideRequestDTO> getAllRideActive();
    List<RideRequestDTO> getRidesByDriverEmail(String email);
    RideRequestDTO findDetailRideById(Long rideId);
    RideRequestDTO cancelRideById(Long rideId,String email);
    public RideRequestDTO updateRide(Long rideId, RideRequestDTO rideRequest, String email);}