package com.maja.complaints.service

import com.maja.complaints.dto.ComplaintRequest
import com.maja.complaints.dto.ComplaintUpdateRequest
import com.maja.complaints.exception.ComplaintNotFoundException
import com.maja.complaints.model.Complaint
import com.maja.complaints.repository.ComplaintRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ComplaintServiceTest extends Specification {

    ComplaintRepository complaintRepository = Mock()
    LocationService locationService = Mock()
    static Clock clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.systemDefault())

    @Subject
    ComplaintService complaintService = new ComplaintService(locationService, complaintRepository, clock)

    def "should create new complaint when no previous complaint exists"() {
        given: "a complaint request"
        def productId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def ip = "8.8.8.8"
        def request = new ComplaintRequest(productId, "Test complaint")

        and: "location service returns a country"
        locationService.locateOrDefault(ip, "NA") >> "US"

        and: "no previous complaint exists"
        complaintRepository.findByCreatedByAndProductId(userId, productId) >> Optional.empty()

        and: "repository saves the complaint"
        complaintRepository.save(_) >> { Complaint complaint ->
            complaint.setId(UUID.randomUUID())
            return complaint
        }

        when: "creating a new complaint"
        def response = complaintService.createComplaint(request, userId, ip)

        then: "the response contains correct data"
        response.productId == productId
        response.content == "Test complaint"
        response.createdBy == userId
        response.country == "US"
        response.complaintCounter == 1
        response.id != null
        response.creationDate != null
    }

    def "should increment counter when complaint exists for same user and product"() {
        given: "a complaint request"
        def productId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def ip = "8.8.8.8"
        def request = new ComplaintRequest(productId, "Another complaint")

        and: "an existing complaint"
        def existingComplaint = Complaint.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .createdBy(userId)
                .content("Original complaint")
                .country("US")
                .complaintCounter(1)
                .creationDate(LocalDateTime.now(clock))
                .build()

        and: "repository returns existing complaint"
        complaintRepository.findByCreatedByAndProductId(userId, productId) >> Optional.of(existingComplaint)

        and: "repository saves the updated complaint"
        complaintRepository.save(_) >> { Complaint complaint -> complaint }

        when: "creating another complaint"
        def response = complaintService.createComplaint(request, userId, ip)

        then: "the response contains updated counter but original data"
        response.productId == productId
        response.content == "Original complaint"
        response.createdBy == userId
        response.country == "US"
        response.complaintCounter == 2
        response.id == existingComplaint.id
        response.creationDate == existingComplaint.creationDate
    }

    def "should successfully update complaint content"() {
        given: "an existing complaint"
        def complaintId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def existingComplaint = createComplaint(complaintId)
        def updateRequest = new ComplaintUpdateRequest("Updated content")

        complaintRepository.findById(complaintId) >> Optional.of(existingComplaint)
        complaintRepository.save(_) >> { Complaint c -> c }

        when: "updating the complaint"
        def response = complaintService.updateComplaint(complaintId, updateRequest, userId)

        then: "the complaint is updated with new content"
        response.id == complaintId
        response.content == "Updated content"
    }

    def "should throw exception when updating non-existent complaint"() {
        given: "a non-existent complaint id"
        def complaintId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def updateRequest = new ComplaintUpdateRequest("Updated content")

        and: "repository returns empty optional"
        complaintRepository.findById(complaintId) >> Optional.empty()

        when: "attempting to update the complaint"
        complaintService.updateComplaint(complaintId, updateRequest, userId)

        then: "ComplaintNotFoundException is thrown"
        thrown(ComplaintNotFoundException)
    }

    def "should return filtered complaints page"() {
        given: "filter parameters"
        def country = "US"
        def productId = UUID.randomUUID()
        def createdBy = UUID.randomUUID()
        def pageable = PageRequest.of(0, 20)

        and: "existing complaints"
        def complaints = [
                createComplaint(UUID.randomUUID(), country, productId, createdBy),
                createComplaint(UUID.randomUUID(), country, productId, createdBy)
        ]
        def complaintsPage = new PageImpl<>(complaints, pageable, complaints.size())
        complaintRepository.findAllByFilters(country, productId, createdBy, pageable) >> complaintsPage

        when: "getting complaints with filters"
        def response = complaintService.getComplaints(country, productId, createdBy, pageable)

        then: "correct page of complaints is returned"
        response.content.size() == 2
        response.totalPages == 1
        response.totalElements == 2
        response.currentPage == 0
    }

    def "should handle empty results when filtering complaints"() {
        given: "filter parameters"
        def pageable = PageRequest.of(0, 20)

        and: "empty page from repository"
        def emptyPage = new PageImpl<Complaint>([], pageable, 0)
        complaintRepository.findAllByFilters(null, null, null, pageable) >> emptyPage

        when: "getting complaints without filters"
        def response = complaintService.getComplaints(null, null, null, pageable)

        then: "empty page is returned"
        response.content.isEmpty()
        response.totalPages == 0
        response.totalElements == 0
        response.currentPage == 0
    }

    def "should apply different filter combinations"() {
        given: "pageable parameter"
        def pageable = PageRequest.of(0, 20)

        complaintRepository.findAllByFilters(country, productId, createdBy, pageable) >> { String c, UUID p, UUID u, Pageable pag ->
            new PageImpl<>([createComplaint(UUID.randomUUID(), c, p, u)], pag, 1)
        }

        when: "getting complaints with different filter combinations"
        def response = complaintService.getComplaints(country, productId, createdBy, pageable)

        then: "correct filtered results are returned"
        response.content.size() == 1
        if (country) {
            response.content[0].country == country
        }
        if (productId) {
            response.content[0].productId == productId
        }
        if (createdBy) {
            response.content[0].createdBy == createdBy
        }

        where:
        country | productId           | createdBy
        "US"    | UUID.randomUUID()  | UUID.randomUUID()
        "DE"    | null               | UUID.randomUUID()
        null    | UUID.randomUUID()  | null
        "PL"    | null               | null
    }

    private static Complaint createComplaint(UUID id, String country = "US", UUID productId = UUID.randomUUID(), UUID createdBy = UUID.randomUUID()) {
        return Complaint.builder()
                .id(id)
                .productId(productId)
                .createdBy(createdBy)
                .content("Test content")
                .country(country)
                .complaintCounter(1)
                .creationDate(LocalDateTime.now(clock))
                .build()
    }

}