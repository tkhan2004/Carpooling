package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ride")
public class RideController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RideService rideService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> searchRides(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) Integer seats){
        List<RideRequestDTO> resultDTO = rideService.searchRides(departure, destination, startTime, seats);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tìm chuyến đi thành công",resultDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<Object>> createRide(@Valid @RequestBody RideRequestDTO rideRequest, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            rideService.createRide(rideRequest, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Tạo chuyến đi thành công", null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }


    @GetMapping("/available")
//    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    public  ResponseEntity<ApiResponse<List<RideRequestDTO>>> getAllRideActive() {
        List<RideRequestDTO> rides = rideService.getAllRideActive();
        ApiResponse<List<RideRequestDTO>> response = (new ApiResponse<>(true,  "success", rides));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getRideById(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);

        RideRequestDTO rides = rideService.findDetailRideById(id);

        if (rides == null) {
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Chuyến đi không tồn tại",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<RideRequestDTO> response = new ApiResponse<>(
                true,
                "Danh sách chuyến đi",
                rides
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<?>> deleteRide(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        RideRequestDTO ride = rideService.cancelRideById(id, email);
        ApiResponse<RideRequestDTO> response = new ApiResponse<>(true,"Huỷ bỏ chuyến đi thành công",ride);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<ApiResponse<?>> updateRide(@PathVariable Long id, @RequestBody RideRequestDTO rideRequest, HttpServletRequest request) {
        try {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        RideRequestDTO ride = rideService.updateRide(id, rideRequest,email);
        ApiResponse<RideRequestDTO> response = new ApiResponse<>(true, "Cập nhật chuyến đi thành công",ride);
        return ResponseEntity.status(HttpStatus.OK).body(response);}
        catch (RuntimeException ex) {
            ApiResponse<RideRequestDTO> response = new ApiResponse<>(false, "Cập nhật chuyến đi thất bại",null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);}
        }
    }


