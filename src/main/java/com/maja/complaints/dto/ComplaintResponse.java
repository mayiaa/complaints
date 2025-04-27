package com.maja.complaints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private UUID id;
    private UUID productId;
    private UUID createdBy;
    private String content;
    private String country;
    private Integer complaintCounter;
    private LocalDateTime creationDate;
}
