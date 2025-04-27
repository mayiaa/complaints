package com.maja.complaints.controller;

import com.maja.complaints.dto.ComplaintRequest;
import com.maja.complaints.dto.ComplaintResponse;
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

    private UUID extractUserIdFromJwt(Jwt jwt) {
        String subject = jwt.getSubject();
        return UUID.fromString(subject);
    }
}
