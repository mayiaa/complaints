package com.maja.complaints.controller


import com.maja.complaints.BaseIT
import com.maja.complaints.dto.ComplaintRequest
import org.springframework.http.MediaType

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ComplaintCreationTest extends BaseIT {

    def "should create a new complaint"() {
        given: "a valid complaint request"
        def productId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def request = new ComplaintRequest(productId, "Product was damaged on arrival")
        def requestJson = objectMapper.writeValueAsString(request)

        def expectedTimestamp = getFixedDateTimeAsString()

        when: "the create complaint endpoint is called"
        def result = mockMvc.perform(
                post("/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response is successful and contains the expected data"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').exists())
                .andExpect(jsonPath('$.productId').value(productId.toString()))
                .andExpect(jsonPath('$.createdBy').value(userId.toString()))
                .andExpect(jsonPath('$.content').value("Product was damaged on arrival"))
                .andExpect(jsonPath('$.country').value("NA"))
                .andExpect(jsonPath('$.complaintCounter').value(1))
                .andExpect(jsonPath('$.creationDate').value(expectedTimestamp))
    }

    def "should return bad request for invalid complaint data"() {
        given: "a complaint request with missing required fields"
        def userId = UUID.randomUUID()
        def request = new ComplaintRequest(null, "")
        def requestJson = objectMapper.writeValueAsString(request)

        when: "the create complaint endpoint is called"
        def result = mockMvc.perform(
                post("/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "a bad request status is returned"
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.productId').value('Product ID is required'))
                .andExpect(jsonPath('$.content').value('Content is required'))
    }

    def "should increase complaintCounter and not change other data when duplicate userId and productId"() {
        given: "a valid complaint request with already saved userId and productId combination"
        def productId = UUID.randomUUID()
        def userId = UUID.randomUUID()

        insertComplaint(productId, userId, "Product was damaged on arrival")

        def request = new ComplaintRequest(productId, "Respond quickly. My product was damaged on arrival")
        def requestJson = objectMapper.writeValueAsString(request)

        def expectedTimestamp = getFixedDateTimeAsString()

        when: "the create complaint endpoint is called"
        def result = mockMvc.perform(
                post("/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response is successful and contains the expected data"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').exists())
                .andExpect(jsonPath('$.productId').value(productId.toString()))
                .andExpect(jsonPath('$.createdBy').value(userId.toString()))
                .andExpect(jsonPath('$.content').value("Product was damaged on arrival"))
                .andExpect(jsonPath('$.country').value("PL"))
                .andExpect(jsonPath('$.complaintCounter').value(2))
                .andExpect(jsonPath('$.creationDate').value(expectedTimestamp))
    }
}
