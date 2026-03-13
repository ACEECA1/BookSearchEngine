package com.app.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.demo.model.UserDTO;
import com.app.demo.model.User;
import com.app.demo.service.AuthService;
import com.app.demo.util.JWTUtil;
import com.app.demo.model.LoginRequest;
import com.app.demo.model.ResponseDTO;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JWTUtil jwtUtil;
    public AuthController(AuthService authService, JWTUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerUser(@RequestBody UserDTO user) {
        User newUser = authService.registerUser(user);
        UserDTO userDTO = new UserDTO(newUser);
        ResponseDTO responseDTO = new ResponseDTO(userDTO);
        responseDTO.setStatus(HttpStatus.CREATED.value());
        return new ResponseEntity<ResponseDTO>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> loginUser(@RequestBody LoginRequest loginRequest) {
        User user = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (user != null) {
            ResponseCookie cookie = jwtUtil.generateTokenCookie(user);
            ResponseDTO responseDTO = new ResponseDTO("Login successful");
            responseDTO.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(responseDTO);
        } else {
            ResponseDTO responseDTO = new ResponseDTO("Invalid email or password");
            responseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logoutUser() {
        ResponseCookie cookie = jwtUtil.clearTokenCookie();
        ResponseDTO responseDTO = new ResponseDTO("Logout successful");
        responseDTO.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseDTO);
    }
}
