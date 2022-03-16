package org.miage.reservationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.reservationservice.boudary.ReservationResource;
import org.miage.reservationservice.boudary.TravelerResource;
import org.miage.reservationservice.boudary.TripResource;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.ReservationInput;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.miage.reservationservice.types.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationRepresentationTests {

    @LocalServerPort
    int port;

    @Autowired
    ReservationResource reservationResource;

    @Autowired
    TravelerResource travelerResource;

    @Autowired
    TripResource tripResource;

    private Traveler traveler;
    private Trip trip1, trip2;

    @BeforeEach
    public void setupContext() {
        RestAssured.port = port;
        reservationResource.deleteAll();
        travelerResource.deleteAll();
        tripResource.deleteAll();
        Traveler traveler = new Traveler(UUID.randomUUID().toString(), "TEST");
        this.traveler = travelerResource.save(traveler);
        Trip trip = new Trip(
                UUID.randomUUID().toString(),
                "METZ",
                "NANCY",
                LocalDateTime.of(2022, 1, 1, 1, 1, 1),
                LocalDateTime.of(2022, 1, 1, 1, 21, 1),
                10.0,
                1,
                0);
        this.trip1 = tripResource.save(trip);
        Trip trip2 = new Trip(
                UUID.randomUUID().toString(),
                "NANCY",
                "METZ",
                LocalDateTime.of(2022, 1, 3, 1, 1, 1),
                LocalDateTime.of(2022, 1, 3, 1, 21, 1),
                10.0,
                10,
                10);
        this.trip2 = tripResource.save(trip2);
    }

    @Test
    void ping() {
        when().get("/reservations").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void getAllReservations() {
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                this.traveler,
                this.trip1,
                false,
                ReservationStatus.PENDING
        );
        reservationResource.save(reservation);
        Reservation reservation2 = new Reservation(
                UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.PENDING
        );
        reservationResource.save(reservation2);
        when().get("/reservations").then().statusCode(HttpStatus.SC_OK)
                .and().assertThat().body("size()", equalTo(2));
    }

    @Test
    void getOneReservation() {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.PENDING);
        reservationResource.save(reservation);
        Response response = when().get("/reservations/" + reservation.getReservationId()).then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("PENDING"));
    }

    @Test
    void getOneReservation_NotFound() {
        when().get("reservations/0").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void saveReservation_BadRequest_NoBody() throws Exception {
        given().body(toJsonString("")).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().response();
    }

    @Test
    void saveReservation_NotFound_NoTraveler() throws Exception {
        ReservationInput reservationInput = new ReservationInput("0", this.trip1.getTripId(), true);
        given().body(toJsonString(reservationInput)).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_NOT_FOUND)
                .extract().response();
    }

    @Test
    void saveReservation_NotFound_NoTrip() throws Exception {
        ReservationInput reservationInput = new ReservationInput(this.traveler.getTravelerId(),"0", true);
        given().body(toJsonString(reservationInput)).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_NOT_FOUND)
                .extract().response();
    }

    @Test
    void saveReservation_BadRequest_ReservationAlreadyExists() throws Exception {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.PENDING);
        reservationResource.save(reservation);
        ReservationInput reservationInput = new ReservationInput(this.traveler.getTravelerId(),this.trip2.getTripId(), true);
        given().body(toJsonString(reservationInput)).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().response();
    }

    @Test
    void saveReservation_BadRequest_NoSeatAvailable() throws Exception {
        ReservationInput reservationInput = new ReservationInput(this.traveler.getTravelerId(), this.trip1.getTripId(), true);
        given().body(toJsonString(reservationInput)).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void saveReservation_Ok() throws Exception {
        ReservationInput reservationInput = new ReservationInput(this.traveler.getTravelerId(), this.trip1.getTripId(), false);
        Response response = given().body(toJsonString(reservationInput)).contentType(ContentType.JSON)
                .when().post("/reservations").then().statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String location = response.getHeader("Location");
        when().get(location).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void cancelReservation_NotFound_NoReservation() {
        when().delete("/reservations/0").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void cancelReservation_BadRequest_ReservationConfirmed() {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.CONFIRMED);
        reservationResource.save(reservation);
        when().delete("/reservations/" + reservation.getReservationId())
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void cancelReservation_Ok() {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.PENDING);
        reservationResource.save(reservation);
        when().delete("/reservations/" + reservation.getReservationId())
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    void confirmReservation_NotFound_NoReservation() {
        when().patch("/reservations/0").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void confirmReservation_BadRequest_ReservationConfirmed() {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.CONFIRMED);
        reservationResource.save(reservation);
        when().patch("/reservations/" + reservation.getReservationId()).then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void confirmReservation_Ok() {
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                this.traveler,
                this.trip2,
                true,
                ReservationStatus.PENDING);
        reservationResource.save(reservation);
        Response response = when().patch("/reservations/" + reservation.getReservationId())
                .then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("CONFIRMED"));
    }

    private String toJsonString(Object r) throws Exception {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(r);
    }
}
