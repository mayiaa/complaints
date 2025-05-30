package com.maja.complaints.controller

import com.maja.complaints.BaseIT

import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.is
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ComplaintListTest extends BaseIT {

    def "should list complaints with pagination"() {
        given: "multiple complaints exist in the system"
        def userId = UUID.randomUUID()
        def productId1 = UUID.randomUUID()
        def productId2 = UUID.randomUUID()

        insertComplaint(productId1, userId, "First complaint")
        insertComplaint(productId2, userId, "Second complaint")

        when: "the get complaints endpoint is called"
        def result = mockMvc.perform(
                get("/complaints")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response contains a paged list of complaints"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.content').isArray())
                .andExpect(jsonPath('$.content.length()').value(2))
                .andExpect(jsonPath('$.totalElements').value(2))
                .andExpect(jsonPath('$.totalPages').value(1))

                .andExpect(jsonPath('$.content[0].productId').value(productId1.toString()))
                .andExpect(jsonPath('$.content[0].createdBy').value(userId.toString()))
                .andExpect(jsonPath('$.content[0].content').value("First complaint"))
                .andExpect(jsonPath('$.content[0].country').value("PL"))
                .andExpect(jsonPath('$.content[0].complaintCounter').value(1))
                .andExpect(jsonPath('$.content[0].creationDate').value(getFixedDateTimeAsString()))

                .andExpect(jsonPath('$.content[1].productId').value(productId2.toString()))
                .andExpect(jsonPath('$.content[1].createdBy').value(userId.toString()))
                .andExpect(jsonPath('$.content[1].content').value("Second complaint"))
                .andExpect(jsonPath('$.content[1].country').value("PL"))
                .andExpect(jsonPath('$.content[1].complaintCounter').value(1))
                .andExpect(jsonPath('$.content[1].creationDate').value(getFixedDateTimeAsString()))
    }

    def "should filter complaints by country"() {
        given: "complaints from different countries exist in the system"
        def userId = UUID.randomUUID()
        def user2Id = UUID.randomUUID()
        def productId = UUID.randomUUID()

        insertComplaint(productId, userId, "Specific product complaint 1", "GB")
        insertComplaint(productId, user2Id, "Specific product complaint 2", "EN")

        when: "the get complaints endpoint is called with country filter"
        def result = mockMvc.perform(
                get("/complaints")
                        .param("country", "GB")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response contains only complaints from the specified country"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.totalElements').value(1))
                .andExpect(jsonPath('$.content[*].country').value(everyItem(is("GB"))))
    }

    def "should filter complaints by productId"() {
        given: "complaints for different products exist in the system"
        def userId = UUID.randomUUID()
        def specificProductId = UUID.randomUUID()

        insertComplaint(specificProductId, userId, "Specific product complaint")

        when: "the get complaints endpoint is called with productId filter"
        def result = mockMvc.perform(
                get("/complaints")
                        .param("productId", specificProductId.toString())
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response contains only complaints for the specified product"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.content[*].productId').value(everyItem(is(specificProductId.toString()))))
    }

    def "should filter complaints by user who created them"() {
        given: "complaints created by different users exist in the system"
        def userId = UUID.randomUUID()
        def user2Id = UUID.randomUUID()
        def productId = UUID.randomUUID()

        insertComplaint(productId, userId, "Product was broken on delivery")
        insertComplaint(productId, user2Id, "Product malfunction")

        when: "the get complaints endpoint is called with createdBy filter"
        def result = mockMvc.perform(
                get("/complaints")
                        .param("createdBy", userId.toString())
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
        )

        then: "the response contains only complaints created by the specified user"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.totalElements').value(1))
                .andExpect(jsonPath('$.content[*].createdBy').value(everyItem(is(userId.toString()))))
    }
}
