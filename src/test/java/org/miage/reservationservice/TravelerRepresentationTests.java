package org.miage.reservationservice;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.reservationservice.boudary.ReservationResource;
import org.miage.reservationservice.boudary.TravelerResource;
import org.miage.reservationservice.boudary.TripResource;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.miage.reservationservice.types.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TravelerRepresentationTests {

    @LocalServerPort
    int port;

    @Autowired
    TravelerResource travelerResource;

    @Autowired
    TripResource tripResource;

    @Autowired
    ReservationResource reservationResource;

    @BeforeEach
    public void setupContext() {
        RestAssured.port = port;
        reservationResource.deleteAll();
        tripResource.deleteAll();
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

    @Test
    void getOneTraveler_NotFound() {
        when().get("/travelers/0").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getOneTraveler() {
        Traveler traveler1 = new Traveler(UUID.randomUUID().toString(), "TEST");
        travelerResource.save(traveler1);
        Response response = when().get("/travelers/" + traveler1.getTravelerId()).then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("TEST"));
    }

    @Test
    void getFavoriteTrips_BadRequest_WrongDate() {
        when().get("/travelers/0/favorites?date=wrong")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getFavoriteTrips_NotFound_NoReservationForThisTraveler() {
        Traveler traveler1 = new Traveler(UUID.randomUUID().toString(), "TEST");
        travelerResource.save(traveler1);
        when().get("/travelers/" + traveler1.getTravelerId() + "/favorites?date=2022-01-01T01:01")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getFavoriteTrips_NotFound_WrongTraveler() {
        when().get("/travelers/0/favorites?date=2022-01-01T01:01")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getFavoriteTrips_NotFound_NoTripAvailable() {
        Traveler traveler = new Traveler(UUID.randomUUID().toString(), "TEST");
        travelerResource.save(traveler);
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 3, 1, 1, 1),
                LocalDateTime.of(2022, 1, 3, 1, 21, 1),
                10.0,
                10,
                10);
        tripResource.save(trip);
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                traveler,
                trip,
                false,
                ReservationStatus.PENDING
        );
        reservationResource.save(reservation);
        when().get("/travelers/0/favorites?date=2022-04-01T01:01")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void getFavoriteTrips_Ok() {
        Traveler traveler = new Traveler(UUID.randomUUID().toString(), "TEST");
        travelerResource.save(traveler);
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 3, 1, 1, 1),
                LocalDateTime.of(2022, 1, 3, 1, 21, 1),
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
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                traveler,
                trip,
                false,
                ReservationStatus.PENDING
        );
        reservationResource.save(reservation);
        when().get("/travelers/" + traveler.getTravelerId() + "/favorites?date=2022-01-10T01:01")
                .then().statusCode(HttpStatus.SC_OK);
    }
}
