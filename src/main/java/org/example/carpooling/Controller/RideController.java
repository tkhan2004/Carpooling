package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ride")
public class RideController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RideService rideService;

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<Object>> createRide(@Valid @RequestBody RideRequestDTO rideRequest, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        rideService.createRide(rideRequest, email);
        ApiResponse<Object> response = new ApiResponse<>(true, "Tạo chuyến đi thành công", HttpStatus.OK);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('DRIVER')")
    public  ResponseEntity<ApiResponse<List<RideRequestDTO>>> getAllRideActive() {
        List<RideRequestDTO> rides = rideService.getAllRideActive();
        ApiResponse<List<RideRequestDTO>> response = (new ApiResponse<>(true,  "success", rides));
        return ResponseEntity.ok(response);
    }
}
