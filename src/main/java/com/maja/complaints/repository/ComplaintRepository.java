package com.maja.complaints.repository;

import com.maja.complaints.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:country IS NULL OR c.country = :country) AND " +
           "(:productId IS NULL OR c.productId = :productId) AND " +
           "(:createdBy IS NULL OR c.createdBy = :createdBy)")
    Page<Complaint> findAllByFilters(
            @Param("country") String country,
            @Param("productId") UUID productId,
            @Param("createdBy") UUID createdBy,
            Pageable pageable);

    Optional<Complaint> findByCreatedByAndProductId(UUID createdBy, UUID productId);
}
