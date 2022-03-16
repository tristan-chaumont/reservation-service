package org.miage.reservationservice;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.reservationservice.boudary.TravelerResource;
import org.miage.reservationservice.entity.Traveler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TravelerRepresentationTests {

    @LocalServerPort
    int port;

    @Autowired
    TravelerResource travelerResource;

    @BeforeEach
    public void setupContext() {
        RestAssured.port = port;
        travelerResource.deleteAll();
    }

    @Test
    void ping() {
        when().get("/travelers").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void getAllTravelers() {
        Traveler traveler1 = new Traveler(UUID.randomUUID().toString(), "TEST");
        Traveler traveler2 = new Traveler(UUID.randomUUID().toString(), "TEST2");
        travelerResource.save(traveler1);
        travelerResource.save(traveler2);
        when().get("/travelers").then().statusCode(HttpStatus.SC_OK)
                .and().assertThat().body("size()", equalTo(2));
    }
}