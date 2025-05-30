package com.maja.complaints.service;

import com.maja.complaints.dto.ComplaintRequest;
import com.maja.complaints.dto.ComplaintResponse;
import com.maja.complaints.dto.ComplaintUpdateRequest;
import com.maja.complaints.dto.PagedComplaintsResponse;
import com.maja.complaints.exception.ComplaintNotFoundException;
import com.maja.complaints.model.Complaint;
import com.maja.complaints.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional
    public ComplaintResponse updateComplaint(UUID complaintId, ComplaintUpdateRequest request, UUID userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + complaintId));

        complaint.setContent(request.getContent());

        Complaint updatedComplaint = complaintRepository.save(complaint);

        return mapToComplaintResponse(updatedComplaint);
    }

    @Transactional(readOnly = true)
    public PagedComplaintsResponse getComplaints(String country, UUID productId, UUID createdBy, Pageable pageable) {
        Page<Complaint> complaintsPage = complaintRepository.findAllByFilters(country, productId, createdBy, pageable);

        List<ComplaintResponse> complaintResponses = complaintsPage.getContent().stream()
                .map(this::mapToComplaintResponse)
                .collect(Collectors.toList());

        return PagedComplaintsResponse.builder()
                .content(complaintResponses)
                .totalPages(complaintsPage.getTotalPages())
                .totalElements(complaintsPage.getTotalElements())
                .currentPage(pageable.getPageNumber())
                .build();
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
