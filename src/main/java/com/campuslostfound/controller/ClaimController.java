
package com.campuslostfound.controller;

import com.campuslostfound.entity.Claim;
import com.campuslostfound.entity.User;
import com.campuslostfound.repository.UserRepository;
import com.campuslostfound.service.ClaimService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final UserRepository userRepository;

    public ClaimController(
            ClaimService claimService,
            UserRepository userRepository) {
        this.claimService = claimService;
        this.userRepository = userRepository;
    }

    // Student or faculty submits a claim
    @PostMapping("/item/{itemId}")
    public ResponseEntity<?> submitClaim(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            User student = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Logged-in user was not found."));

            String reason = request.get("reason");

            Claim claim = claimService.submitClaim(
                    itemId,
                    student.getId(),
                    reason
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(claim);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Student or faculty views their own claims
    @GetMapping("/my")
    public ResponseEntity<?> getMyClaims(
            Authentication authentication) {

        String email = authentication.getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in user was not found."));

        List<Claim> claims =
                claimService.getClaimsForStudent(student.getId());

        return ResponseEntity.ok(claims);
    }

    // Admin views all pending claims
    @GetMapping("/admin/pending")
    public ResponseEntity<List<Claim>> getPendingClaims() {

        List<Claim> claims = claimService.getPendingClaims();

        return ResponseEntity.ok(claims);
    }

    // Admin approves a claim
    @PutMapping("/admin/{claimId}/approve")
    public ResponseEntity<?> approveClaim(
            @PathVariable Long claimId) {

        try {
            Claim claim = claimService.approveClaim(claimId);
            return ResponseEntity.ok(claim);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Admin rejects a claim
    @PutMapping("/admin/{claimId}/reject")
    public ResponseEntity<?> rejectClaim(
            @PathVariable Long claimId) {

        try {
            Claim claim = claimService.rejectClaim(claimId);
            return ResponseEntity.ok(claim);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/my-posted-items")
public ResponseEntity<?> getClaimsForMyPostedItems(
        Authentication authentication) {

    String email = authentication.getName();

    User poster = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Logged-in user was not found."));

    List<Claim> claims =
            claimService.getClaimsForPostedItems(poster.getId());

    return ResponseEntity.ok(claims);
}
}
