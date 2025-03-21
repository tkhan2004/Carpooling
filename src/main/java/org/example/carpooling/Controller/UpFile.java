package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

@RestController
@RequestMapping("/upload")
public class UpFile {
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/avatar")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return handleFileUpLoad(file, request, "license");
    }

    @PostMapping("/license")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> uploadLicense(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return handleFileUpLoad(file, request, "license");
    }

    @PostMapping("/vehicle")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> uploadVehicle(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return handleFileUpLoad(file, request, "vehicle");

    }

    public ResponseEntity<?> handleFileUpLoad(MultipartFile file, HttpServletRequest request, String type) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionallUser = userRepository.findByEmail(email);

        if(optionallUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy tài xế ");
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadPath = request.getServletContext().getRealPath("/upload");
        String filePath = uploadPath + fileName;
        try {
            File dis = new File(filePath);

            Users user = optionallUser.get();
            switch (type)
            {
                case "avatar" -> user.setAvatarImage(filePath);
                case "license" -> user.setLicenseImageUrl(filePath);
                case "vehicle" -> user.setLicenseImageUrl(filePath);
            }
            userRepository.save(user);
            return ResponseEntity.ok().body("Tải ảnh lên thành công" +type);
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tải " + type);
        }


    }
}
