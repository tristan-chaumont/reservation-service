package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Reservation;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Reservation.class)
public class ReservationRepresentation {

    private final ReservationResource rr;

    public ReservationRepresentation(ReservationResource rr) {
        this.rr = rr;
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(rr.findAll());
    }

    /**
     * Get all reservations from a certain time.
     * @return Reservations from time wanted.
     */
    @GetMapping("/{time}")
    public ResponseEntity<List<Reservation>> getReservationsFromTime(@PathVariable("time") String time) {
        return null;
    }
}
