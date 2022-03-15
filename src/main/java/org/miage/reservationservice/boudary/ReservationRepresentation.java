package org.miage.reservationservice.boudary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.ReservationInput;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.miage.reservationservice.types.ReservationStatus;
import org.miage.reservationservice.exception.APIException;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Reservation.class)
public class ReservationRepresentation {

    private final ReservationResource rr;
    private final TravelerResource trar;
    private final TripResource trir;

    public ReservationRepresentation(ReservationResource rr, TravelerResource trar, TripResource trir) {
        this.rr = rr;
        this.trar = trar;
        this.trir = trir;
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(rr.findAll());
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<Reservation> getOneReservation(@PathVariable String reservationId) {
        return Optional.of(rr.findById(reservationId))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(i.get()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<URI> saveReservation(@RequestBody @Valid ReservationInput reservationInput) {
        Optional<ReservationInput> body = Optional.ofNullable(reservationInput);
        if (body.isEmpty()) return ResponseEntity.badRequest().build();
        Optional<Traveler> traveler = trar.findById(reservationInput.getTravelerId());
        if (traveler.isEmpty())
            throw new APIException(404, "Le numéro du voyageur spécifié n'existe pas");
        Optional<Trip> trip = trir.findById(reservationInput.getTripId());
        if (trip.isEmpty())
            throw new APIException(404, "Le numéro du trajet spécifié n'existe pas");
        Optional<Reservation> reservation = rr.findByTravelerAndTrip(traveler.get(), trip.get());
        if (reservation.isPresent())
            throw new APIException(400, "Une réservation pour ce voyageur et ce trajet existe déjà");
        Reservation toSave = new Reservation(UUID.randomUUID().toString(),
                traveler.get(),
                trip.get(),
                reservationInput.isWindowSeat(),
                ReservationStatus.PENDING);
        Reservation saved = rr.save(toSave);
        URI location = linkTo(ReservationRepresentation.class).slash(saved.getReservationId()).toUri();
        return ResponseEntity.created(location).build();
    }
}
