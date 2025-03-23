package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.ChangePassDTO;
import org.example.carpooling.Dto.UserUpdateDTO;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Service.Imp.UserServiceImp;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController
{
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PutMapping("/change-pass")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<?> changePass(@RequestBody ChangePassDTO  changePassDTO, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String mgs = userService.changePass(token,  changePassDTO);
        return ResponseEntity.ok(mgs);
    }

    @PutMapping("/update-profile")
    public  ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String mgs = userService.updateProfile(token,  userUpdateDTO);
        return ResponseEntity.ok(mgs);}

}
