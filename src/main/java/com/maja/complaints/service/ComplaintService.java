package com.maja.complaints.service;

import com.maja.complaints.dto.ComplaintRequest;
import com.maja.complaints.dto.ComplaintResponse;
import com.maja.complaints.model.Complaint;
import com.maja.complaints.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private static final String DEFAULT_COUNTRY_VALUE = "NA";

    private final LocationService locationService;
    private final ComplaintRepository complaintRepository;
    private final Clock clock;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request, UUID userId, String ip) {

        var complaintToBeSaved = complaintRepository.findByCreatedByAndProductId(userId, request.getProductId())
                .flatMap(complaint -> {
                    complaint.setComplaintCounter(complaint.getComplaintCounter() + 1);
                    return Optional.of(complaint);
                })
                .orElseGet(() -> getNewComplaint(request, userId, ip));

        Complaint savedComplaint = complaintRepository.save(complaintToBeSaved);

        return mapToComplaintResponse(savedComplaint);
    }

    private ComplaintResponse mapToComplaintResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .productId(complaint.getProductId())
                .content(complaint.getContent())
                .creationDate(complaint.getCreationDate())
                .createdBy(complaint.getCreatedBy())
                .country(complaint.getCountry())
                .complaintCounter(complaint.getComplaintCounter())
                .build();
    }

    private Complaint getNewComplaint(ComplaintRequest request, UUID userId, String ip) {
        var countryCode = locationService.locateOrDefault(ip, DEFAULT_COUNTRY_VALUE);

        return Complaint.builder()
                .id(UUID.randomUUID())
                .productId(request.getProductId())
                .content(request.getContent())
                .creationDate(LocalDateTime.now(clock))
                .createdBy(userId)
                .country(countryCode)
                .complaintCounter(1)
                .build();
    }
}
