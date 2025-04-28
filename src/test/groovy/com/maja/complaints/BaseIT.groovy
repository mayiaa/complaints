package com.maja.complaints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.maja.complaints.config.TestClockConfig
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Clock
import java.time.LocalDateTime

@Import(TestClockConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@Testcontainers
abstract class BaseIT extends Specification {

    @Shared
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer<>(org.testcontainers.utility.DockerImageName.parse("postgres:latest"))
            .withReuse(true);

    @Autowired
    protected Clock fixedClock;

    @Autowired
    WebApplicationContext wac

    @Autowired
    TransactionTemplate transactionTemplate

    MockMvc mockMvc

    def setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build()
    }

    @Shared
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    def setupSpec() {
        postgres.start()
    }
    @Autowired
    DataSource dataSource

    @PersistenceContext
    EntityManager entityManager

    def cleanup() {
        transactionTemplate.execute(
                status -> entityManager.createNativeQuery("TRUNCATE TABLE complaints").executeUpdate())
    }

    protected insertComplaint(UUID productId, UUID userId, String content, String country = "PL") {
        def creationDate = LocalDateTime.now(fixedClock)

        transactionTemplate.execute({ status ->
            entityManager.createNativeQuery("INSERT INTO complaints (id, product_id, created_by, content, country, complaint_counter, creation_date) VALUES (:id, :productId, :userId, :content, :country, 1, :creationDate)")
                    .setParameter("id", UUID.randomUUID())
                    .setParameter("productId", productId)
                    .setParameter("userId", userId)
                    .setParameter("content", content)
                    .setParameter("creationDate", creationDate)
                    .setParameter("country", country)
                    .executeUpdate()
        })
    }

    protected String getFixedDateTimeAsString() {
        LocalDateTime.now(fixedClock).toString()
    }
}