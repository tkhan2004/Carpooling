package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.Request.RideRequestDTO;
import org.example.carpooling.Dto.Response.RideResponseDTO;
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
@Tag(name = "Rides", description = "API quản lý chuyến đi")
public class RideController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RideService rideService;

    @Operation(
            summary = "Tìm kiếm chuyến đi",
            description = "Hành khách có thể tìm kiếm chuyến đi theo điểm đi, điểm đến, ngày khởi hành và số ghế trống."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm chuyến đi thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi khi tìm chuyến đi")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> searchRides(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) Integer seats) {
        try {
            List<RideRequestDTO> resultDTO = rideService.searchRides(departure, destination, startTime, seats);
            return ResponseEntity.ok(new ApiResponse<>(true, "Tìm chuyến đi thành công", HttpStatus.OK.value(), resultDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi tìm chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Tạo chuyến đi",
            description = "Tài xế tạo một chuyến đi mới với thông tin điểm đi, điểm đến, thời gian và số ghế."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo chuyến đi thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi tạo chuyến đi")
    })
    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<Object>> createRide(@Valid @RequestBody RideRequestDTO rideRequest, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            rideService.createRide(rideRequest, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Tạo chuyến đi thành công", HttpStatus.CREATED.value(), null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi hệ thống khi tạo chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Danh sách chuyến đi đang hoạt động",
            description = "Lấy tất cả chuyến đi còn hiệu lực (chưa hủy, chưa hoàn thành)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi khi lấy danh sách chuyến đi")
    })
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> getAllRideActive() {
        try {
            List<RideRequestDTO> rides = rideService.getAllRideActive();
            return ResponseEntity.ok(new ApiResponse<>(true, "success", HttpStatus.OK.value(), rides));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Xem chi tiết chuyến đi",
            description = "Xem thông tin chi tiết của chuyến đi theo ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách chuyến đi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Chuyến đi không tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi khi lấy thông tin chuyến đi")
    })

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getRideById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            RideResponseDTO rides = rideService.findDetailRideById(id);

            if (rides == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Chuyến đi không tồn tại", HttpStatus.NOT_FOUND.value(), null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách chuyến đi", HttpStatus.OK.value(), rides));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy thông tin chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Huỷ chuyến đi",
            description = "Tài xế có thể huỷ một chuyến đi theo ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Huỷ bỏ chuyến đi thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi huỷ chuyến đi")
    })
    @PutMapping("cancel/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<?>> deleteRide(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            RideRequestDTO ride = rideService.cancelRideById(id, email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Huỷ bỏ chuyến đi thành công", HttpStatus.OK.value(), ride));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi hệ thống khi huỷ chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Cập nhật chuyến đi",
            description = "Tài xế cập nhật thông tin chuyến đi đã tạo."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật chuyến đi thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi cập nhật chuyến đi")
    })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<ApiResponse<?>> updateRide(@PathVariable Long id, @RequestBody RideRequestDTO rideRequest, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            rideService.updateRide(id, rideRequest, email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật chuyến đi thành công", HttpStatus.OK.value(), null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi hệ thống khi cập nhật chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(
            summary = "Lấy tất cả chuyến đi",
            description = "Trả về danh sách toàn bộ chuyến đi trong hệ thống (dành cho quản trị hoặc tài xế)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tất cả chuyến đi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi khi lấy tất cả chuyến đi")
    })
    @GetMapping("/all-rides")
    public ResponseEntity<ApiResponse<?>> getAllRide(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            List<RideRequestDTO> requestDTOS = rideService.getAllRides();
            return ResponseEntity.ok(new ApiResponse<>(true, "Tất cả chuyến đi", HttpStatus.OK.value(), requestDTOS));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy tất cả chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

}
