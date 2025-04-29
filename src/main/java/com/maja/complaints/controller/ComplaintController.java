package com.maja.complaints.controller;

import com.maja.complaints.dto.ComplaintRequest;
import com.maja.complaints.dto.ComplaintResponse;
import com.maja.complaints.dto.ComplaintUpdateRequest;
import com.maja.complaints.exception.ComplaintNotFoundException;
import com.maja.complaints.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            HttpServletRequest request, @RequestBody @Valid ComplaintRequest complaintRequest,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = extractUserIdFromJwt(jwt);
        String ip = request.getRemoteAddr();

        ComplaintResponse response = complaintService.createComplaint(complaintRequest, userId, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{complaintId}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable UUID complaintId,
            @RequestBody ComplaintUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            UUID userId = extractUserIdFromJwt(jwt);
            ComplaintResponse response = complaintService.updateComplaint(complaintId, request, userId);
            return ResponseEntity.ok(response);
        } catch (ComplaintNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private UUID extractUserIdFromJwt(Jwt jwt) {
        String subject = jwt.getSubject();
        return UUID.fromString(subject);
    }
}
