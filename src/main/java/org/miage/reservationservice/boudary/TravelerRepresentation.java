package org.miage.reservationservice.boudary;

import org.miage.reservationservice.control.ReservationAssembler;
import org.miage.reservationservice.control.TravelerAssembler;
import org.miage.reservationservice.control.TripAssembler;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.miage.reservationservice.exception.APIException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping(value = "/travelers", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Traveler.class)
public class TravelerRepresentation {

    private final TravelerResource tr;
    private final TravelerAssembler ta;
    private final ReservationResource rr;
    private final ReservationAssembler ra;
    private final TripResource trir;
    private final TripAssembler tra;

    public TravelerRepresentation(
            TravelerResource tr,
            TravelerAssembler ta,
            ReservationAssembler ra,
            ReservationResource rr,
            TripResource trir,
            TripAssembler tra) {
        this.tr = tr;
        this.ta = ta;
        this.ra = ra;
        this.rr = rr;
        this.trir = trir;
        this.tra = tra;
    }

    @GetMapping
    public ResponseEntity<List<Traveler>> getTravelers() {
        return ResponseEntity.ok(tr.findAll());
    }

    @GetMapping("/{travelerId}")
    public ResponseEntity<EntityModel<Traveler>> getOneTraveler(@PathVariable String travelerId) {
        return Optional.of(tr.findById(travelerId))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ta.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{travelerId}/reservations")
    public ResponseEntity<CollectionModel<EntityModel<Reservation>>> getTravelerReservations(@PathVariable String travelerId) {
        var traveler = tr.findById(travelerId);
        if (traveler.isEmpty())
            throw new APIException(404, "Le voyageur spécifié n'existe pas");
        var existingTraveler = traveler.get();
        return ResponseEntity.ok(ra.toCollectionModel(existingTraveler.getReservations()));
    }

    @GetMapping("/{travelerId}/favorites")
    public ResponseEntity<CollectionModel<EntityModel<Trip>>> getFavoriteTrips(@PathVariable String travelerId, @RequestParam String date) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(date);
        } catch (DateTimeException e) {
            throw new APIException(400, "La date de départ doit être au format yyyy-DD-mmTHH:mm et être valide. Exemple : '2022-03-25T11:30'");
        }

        var reservations = rr.findByTravelerTravelerId(travelerId);
        if (reservations.isEmpty()) {
            throw new APIException(404, "Cet utilisateur n'existe pas ou n'a jamais effectué de voyage");
        }

        var trips = new HashSet<Trip>();
        reservations.forEach(r -> {
            var trip = trir.findById(r.getTrip().getTripId());
            trip.ifPresent(t -> trips.addAll(trir.findAllByDepartureCityAndArrivalCity(t.getDepartureCity(), t.getArrivalCity())));
        });

        var filteredTrips = trips.stream().filter(t -> {
            var tripTime = t.getDepartureTime();
            return tripTime.isAfter(dateTime) && tripTime.getDayOfMonth() == dateTime.getDayOfMonth();
        }).toList();

        if (filteredTrips.isEmpty())
            throw new APIException(404, "Il n'y a pas de voyage disponible actuellement avec vos préférences");

        return ResponseEntity.ok(tra.toCollectionModel(filteredTrips));
    }
}
