package org.miage.reservationservice;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.reservationservice.boudary.TripResource;
import org.miage.reservationservice.entity.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TripRepresentationTests {

    @LocalServerPort
    int port;

    @Autowired
    TripResource tripResource;

    @BeforeEach
    public void setupContext() {
        RestAssured.port = port;
        tripResource.deleteAll();
    }

    @Test
    void ping() {
        when().get("/trips").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void getTripsForTime_BadRequest_Type() {
        when().get("/trips/testA/testB?type=wrong&date=2022-01-01T01:01&windowSeat=true").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getTripsForTime_BadRequest_Date() {
        when().get("/trips/testA/testB?date=wrong&windowSeat=true").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getTripsForTime_BadRequest_WindowSeat() {
        when().get("/trips/testA/testB?date=2022-01-01T01:01&windowSeat=wrong").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsAller() {
        when().get("/trips/Nancy/Metz?date=2022-01-01T01:01&windowSeat=true").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsAller_CauseNoSeat() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                1,
                0);
        tripResource.save(trip);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsAller_CauseNoTripsToday() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 2, 1, 1, 1),
                LocalDateTime.of(2022, 1, 2, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsRetour() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true&type=aller-retour").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsRetour_CauseNoSeat() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        Trip trip2 = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 2, 1, 1, 1),
                LocalDateTime.of(2022, 1, 2, 1, 21, 1),
                10.0,
                0,
                0);
        tripResource.save(trip2);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true&type=aller-retour").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_NotFound_NoTripsRetour_CauseNoTripsNextWeek() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        Trip trip2 = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 10, 1, 1, 1),
                LocalDateTime.of(2022, 1, 10, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip2);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true&type=aller-retour").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getTripsForTime_Ok_Aller() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 2, 20, 1),
                LocalDateTime.of(2022, 1, 1, 2, 50, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true").then().statusCode(HttpStatus.SC_OK)
                .and().assertThat().body("size()", equalTo(1));
    }

    @Test
    void getTripsForTime_Ok_AllerRetour() {
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        Trip trip2 = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 3, 1, 1, 1),
                LocalDateTime.of(2022, 1, 3, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip2);
        when().get("/trips/Metz/Nancy?date=2022-01-01T01:00&windowSeat=true&type=aller-retour").then().statusCode(HttpStatus.SC_OK)
                .and().assertThat().body("size()", equalTo(1));
    }
}
