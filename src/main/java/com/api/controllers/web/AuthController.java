package com.api.controllers.web;

import com.api.dto.request.LoginRequestDTO;
import com.api.dto.request.RegisterRequestDTO;
import com.api.dto.response.AuthResponseDTO;
import com.api.services.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    final private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> resgiter(@Valid @RequestBody RegisterRequestDTO request){
        String response =  authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmAccount(@RequestParam("token") String token){
        String response = authService.confirmAccount(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
