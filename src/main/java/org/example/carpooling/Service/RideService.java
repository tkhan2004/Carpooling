package org.example.carpooling.Service;

import org.example.carpooling.Dto.Request.RideRequestDTO;
import org.example.carpooling.Dto.Response.RideResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface RideService {
    public void createRide(RideRequestDTO rideRequest, String email) ;
    List<RideRequestDTO> getAllRideActive();
    List<RideRequestDTO> getRidesByDriverEmail(String email);
    RideResponseDTO findDetailRideById(Long rideId);
    RideRequestDTO cancelRideById(Long rideId,String email);
    public void updateRide(Long rideId, RideRequestDTO rideRequest, String email);
    List<RideRequestDTO> searchRides(String departure,
                                     String destination,
                                     LocalDate startTime,
                                      Integer seats);
    public List<RideRequestDTO> getAllRides();

}