package org.miage.reservationservice.boudary;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.miage.reservationservice.control.ReservationAssembler;
import org.miage.reservationservice.entity.*;
import org.miage.reservationservice.service.BankAPI;
import org.miage.reservationservice.types.ReservationStatus;
import org.miage.reservationservice.exception.APIException;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Reservation.class)
public class ReservationRepresentation {

    private final ReservationResource rr;
    private final ReservationAssembler ra;
    private final TravelerResource trar;
    private final TripResource trir;
    private final BankAPI bankApi;

    public ReservationRepresentation(ReservationResource rr, ReservationAssembler ra, TravelerResource trar, TripResource trir, BankAPI bankApi) {
        this.rr = rr;
        this.ra = ra;
        this.trar = trar;
        this.trir = trir;
        this.bankApi = bankApi;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Reservation>>> getAllReservations() {
        return ResponseEntity.ok(ra.toCollectionModel(rr.findAll()));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<EntityModel<Reservation>> getOneReservation(@PathVariable String reservationId) {
        return Optional.of(rr.findById(reservationId))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ra.toModel(i.get())))
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

    @DeleteMapping("/{reservationId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
        var reservation = rr.findById(reservationId);
        if (reservation.isEmpty())
            throw new APIException(404, "La réservation n'existe pas");
        var status = reservation.get().getStatus();
        if (status == ReservationStatus.CONFIRMED || status == ReservationStatus.PAID)
            throw new APIException(400, "Impossible d'annuler une réservation déjà confirmée ou payée");
        rr.delete(reservation.get());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reservationId}/confirm")
    @Transactional
    public ResponseEntity<EntityModel<Reservation>> confirmReservation(@PathVariable String reservationId) {
        var reservation = rr.findById(reservationId);
        if (reservation.isEmpty()) {
            throw new APIException(404, "La réservation n'existe pas");
        }
        var status = reservation.get().getStatus();
        if (status == ReservationStatus.CONFIRMED || status == ReservationStatus.PAID) {
            throw new APIException(400, "La réservation a déjà été confirmée ou payée");
        }
        Reservation existingReservation = reservation.get();
        existingReservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = rr.save(existingReservation);
        return ResponseEntity.ok(ra.toModel(saved));
    }

    @PatchMapping("/{reservationId}/pay")
    @Transactional
    public ResponseEntity<BankResponse> payReservation(@PathVariable String reservationId) {
        var reservation = rr.findById(reservationId);
        if (reservation.isEmpty()) {
            throw new APIException(404, "La réservation n'existe pas");
        }
        var status = reservation.get().getStatus();
        if (status == ReservationStatus.PENDING || status == ReservationStatus.PAID) {
            throw new APIException(400, "La réservation n'a pas été confirmée ou est déjà payée");
        }

        Reservation existingReservation = reservation.get();

        BankResponse response = bankApi.callBankAPI(existingReservation.getTraveler().getName(), existingReservation.getTrip().getPrice());

        if (response.isPaymentAuthorized()) {
            existingReservation.setStatus(ReservationStatus.PAID);
            rr.save(existingReservation);
            return ResponseEntity.ok(response);
        } else {
            throw new APIException(400, "Réservation non autorisée");
        }
    }
}
