
package com.campuslostfound.repository;

import com.campuslostfound.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Claims submitted by a student
    List<Claim> findByStudentId(Long studentId);

    // Claims filtered by status
    List<Claim> findByStatus(String status);

    // Claims for a particular item
    List<Claim> findByItemId(Long itemId);

    // Claims on items posted by a particular user
    List<Claim> findByItemPostedById(Long posterId);
}
