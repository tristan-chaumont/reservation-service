package org.miage.reservationservice.boudary;

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
        Trip existingTrip = trip.get();
        boolean isSeatAvailable = (reservationInput.isWindowSeat() && existingTrip.getNumWindow() > 0) ||
                (!reservationInput.isWindowSeat() && existingTrip.getNumCorridor() > 0);
        if (!isSeatAvailable)
            throw new APIException(400, String.format("Il n'y a plus de place côté %s pour ce voyage", reservationInput.isWindowSeat() ? "fenêtre" : "couloir"));
        existingTrip.decrementSeat(reservationInput.isWindowSeat());
        trir.save(existingTrip);
        Reservation toSave = new Reservation(UUID.randomUUID().toString(),
                traveler.get(),
                existingTrip,
                reservationInput.isWindowSeat(),
                ReservationStatus.PENDING);
        Reservation saved = rr.save(toSave);
        URI location = linkTo(ReservationRepresentation.class).slash(saved.getReservationId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{reservationId}")
    @Transactional
    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
        var reservation = rr.findById(reservationId);
        if (reservation.isEmpty())
            throw new APIException(404, "La réservation n'existe pas");
        var status = reservation.get().getStatus();
        if (status == ReservationStatus.CONFIRMED)
            throw new APIException(400, "Impossible d'annuler une réservation déjà confirmée");
        rr.delete(reservation.get());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reservationId}")
    @Transactional
    public ResponseEntity<Reservation> confirmReservation(@PathVariable String reservationId) {
        var reservation = rr.findById(reservationId);
        if (reservation.isEmpty()) {
            throw new APIException(404, "La réservation n'existe pas");
        }
        var status = reservation.get().getStatus();
        if (status == ReservationStatus.CONFIRMED) {
            throw new APIException(400, "La réservation a déjà été confirmée");
        }
        Reservation existingReservation = reservation.get();
        existingReservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = rr.save(existingReservation);
        return ResponseEntity.ok(saved);
    }
}
