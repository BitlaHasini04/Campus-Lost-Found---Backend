package com.campuslostfound.controller;
import com.campuslostfound.repository.UserRepository;
import java.util.Optional;
import com.campuslostfound.dto.RegisterRequest;
import com.campuslostfound.dto.LoginRequest;
import com.campuslostfound.entity.User;
import com.campuslostfound.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    public AuthController(
        AuthService authService,
        UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
}

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    try {
        User user = authService.register(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }
}
    
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    String token = authService.login(request);

    User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() ->
                    new RuntimeException("User not found."));

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Login successful");
    response.put("token", token);
    response.put("name", user.getName());
    response.put("email", user.getEmail());
    response.put("role", user.getRole());

    return ResponseEntity.ok(response);
}
}