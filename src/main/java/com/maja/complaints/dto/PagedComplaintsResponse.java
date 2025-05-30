package com.maja.complaints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedComplaintsResponse {
    private List<ComplaintResponse> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
