
package com.campuslostfound.service;

import com.campuslostfound.dto.LoginRequest;
import com.campuslostfound.dto.RegisterRequest;
import com.campuslostfound.entity.User;
import com.campuslostfound.repository.UserRepository;
import com.campuslostfound.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER
    public User register(RegisterRequest request) {

        // Check college email
        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "College email is required.");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // Allow only Anurag University email addresses
        if (!email.matches(
                "^[a-zA-Z0-9._%+-]+@anurag\\.edu\\.in$")) {

            throw new RuntimeException(
                    "Only @anurag.edu.in email addresses are allowed.");
        }

        // Check name
        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Student or faculty name is required.");
        }

        // Check password
        if (request.getPassword() == null ||
                request.getPassword().length() < 6) {

            throw new RuntimeException(
                    "Password must contain at least 6 characters.");
        }

        // Prevent duplicate accounts
        if (userRepository.findByEmail(email).isPresent()) {

            throw new RuntimeException(
                    "Email already registered.");
        }

        // Allow only Student or Faculty registration
        String role = request.getRole();

        if (role == null ||
                (!role.equalsIgnoreCase("STUDENT")
                && !role.equalsIgnoreCase("FACULTY"))) {

            throw new RuntimeException(
                    "Please select Student or Faculty.");
        }

        // Create and save user
        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(role.toUpperCase());

        return userRepository.save(user);
    }

    // LOGIN
    public String login(LoginRequest request) {

        if (request.getEmail() == null ||
                request.getPassword() == null) {

            throw new RuntimeException(
                    "Email and password are required.");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password.");
        }

        // Generate JWT after successful login
        System.out.println("Login credentials verified; generating JWT...");
return jwtService.generateToken(user.getEmail(), user.getRole());
    }
}
