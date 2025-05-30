package com.maja.complaints.service

import org.springframework.core.io.ClassPathResource
import spock.lang.Specification
import spock.lang.Subject

class LocationServiceTest extends Specification {

    @Subject
    LocationServiceImpl locationService

    def setup() {
        def databasePath = new ClassPathResource("GeoLite2-Country.mmdb").getFile().getAbsolutePath()
        locationService = new LocationServiceImpl(databasePath)
    }

    def "should return country code for given IP address"() {
        given: "IP address"
        def ipAddress = "8.8.8.8"
        def testFallbackValue = "NA"

        when: "locating the IP address"
        def result = locationService.locateOrDefault(ipAddress, testFallbackValue)

        then: "US country code is returned"
        result == "US"
    }

    def "should return correct fallback value"() {
        expect: "correct country codes for various IP addresses"
        locationService.locateOrDefault(ip, fallback) == expectedCodeResult

        where:
        ip            | fallback | expectedCodeResult
        "192.168.1.1" | "NA"     | "NA"
        "127.0.0.1"   | "NA"     | "NA"
        ""            | "NA"     | "NA"
        null          | "NA"     | "NA"
        "invalid"     | "NA"     | "NA"
    }
}