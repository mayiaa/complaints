package com.maja.complaints.service

import com.maja.complaints.dto.ComplaintRequest
import com.maja.complaints.dto.ComplaintUpdateRequest
import com.maja.complaints.exception.ComplaintNotFoundException
import com.maja.complaints.model.Complaint
import com.maja.complaints.repository.ComplaintRepository
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