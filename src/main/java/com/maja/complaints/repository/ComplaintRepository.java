package com.maja.complaints.repository;

import com.maja.complaints.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Optional<Complaint> findByCreatedByAndProductId(UUID createdBy, UUID productId);
}
