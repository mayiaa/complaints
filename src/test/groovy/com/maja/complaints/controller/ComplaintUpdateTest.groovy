package com.maja.complaints.controller

import com.jayway.jsonpath.JsonPath
import com.maja.complaints.BaseIT
import com.maja.complaints.dto.ComplaintRequest
import com.maja.complaints.dto.ComplaintUpdateRequest
import org.springframework.http.MediaType

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ComplaintUpdateTest extends BaseIT {

    def "should update an existing complaint"() {
        given: "an existing complaint and update request"
        def productId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def initialRequest = new ComplaintRequest(productId, "Initial complaint content")
        def initialRequestJson = objectMapper.writeValueAsString(initialRequest)

        // todo insert?
        def createResult = mockMvc.perform(
                post("/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initialRequestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        def complaintId = JsonPath.read(createResult.andReturn().response.contentAsString, '$.id')

        def updateRequest = new ComplaintUpdateRequest("Updated complaint content")
        def updateRequestJson = objectMapper.writeValueAsString(updateRequest)

        when: "the update complaint endpoint is called"
        def result = mockMvc.perform(
                put("/complaints/${complaintId}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response is successful and contains the updated data"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(complaintId))
                .andExpect(jsonPath('$.productId').value(productId.toString()))
                .andExpect(jsonPath('$.createdBy').value(userId.toString()))
                .andExpect(jsonPath('$.content').value("Updated complaint content"))
                .andExpect(jsonPath('$.country').value("NA"))
                .andExpect(jsonPath('$.complaintCounter').value(1))
                .andExpect(jsonPath('$.creationDate').value(getFixedDateTimeAsString()))
    }

    def "should return not found when updating non-existent complaint"() {
        given: "a non-existent complaint ID and an update request"
        def nonExistentComplaintId = UUID.randomUUID()
        def userId = UUID.randomUUID()
        def updateRequest = new ComplaintUpdateRequest("Updated content")
        def updateRequestJson = objectMapper.writeValueAsString(updateRequest)

        when: "the update complaint endpoint is called with a non-existent ID"
        def result = mockMvc.perform(
                put("/complaints/${nonExistentComplaintId}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "a not found status is returned"
        result.andExpect(status().isNotFound())
    }

}
