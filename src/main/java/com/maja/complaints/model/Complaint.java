package com.maja.complaints.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    
    @Id
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false, length = 2)
    private String country;
    
    @Column(name = "complaint_counter", nullable = false)
    private Integer complaintCounter;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
}
