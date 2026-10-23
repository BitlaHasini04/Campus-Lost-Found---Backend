
package com.campuslostfound.service;

import com.campuslostfound.entity.Claim;
import com.campuslostfound.entity.Item;
import com.campuslostfound.entity.User;
import com.campuslostfound.repository.ClaimRepository;
import com.campuslostfound.repository.ItemRepository;
import com.campuslostfound.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ClaimService(
            ClaimRepository claimRepository,
            ItemRepository itemRepository,
            UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Claim submitClaim(Long itemId, Long studentId, String reason) {

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please explain why this item belongs to you.");
        }

        if (reason.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "Your explanation must be under 1000 characters.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found."));

        if (!"FOUND".equalsIgnoreCase(item.getType())) {
            throw new IllegalArgumentException(
                    "You can only claim a found item.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setStudent(student);
        claim.setReason(reason.trim());
        claim.setStatus("PENDING");

        return claimRepository.save(claim);
    }

    public List<Claim> getClaimsForStudent(Long studentId) {
        return claimRepository.findByStudentId(studentId);
    }

    public List<Claim> getPendingClaims() {
        return claimRepository.findByStatus("PENDING");
    }

    @Transactional
    public Claim approveClaim(Long claimId) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Claim not found."));

        if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending claims can be approved.");
        }

        Item item = claim.getItem();
        item.setStatus("RETURNED");

        claim.setStatus("APPROVED");

        itemRepository.save(item);
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim rejectClaim(Long claimId) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Claim not found."));

        if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending claims can be rejected.");
        }

        claim.setStatus("REJECTED");

        return claimRepository.save(claim);
    }
    public List<Claim> getClaimsForPostedItems(Long posterId) {
    return claimRepository.findByItemPostedById(posterId);
}
}
