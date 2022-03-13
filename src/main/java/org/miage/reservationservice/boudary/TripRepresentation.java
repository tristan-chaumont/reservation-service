package org.miage.reservationservice.boudary;

import org.miage.reservationservice.control.TripAssembler;
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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/trips", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Trip.class)
public class TripRepresentation {

    private final TripResource tr;
    private final TripAssembler ta;

    public TripRepresentation(TripResource tr, TripAssembler ta) {
        this.tr = tr;
        this.ta = ta;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Trip>>> getTrips() {
        return ResponseEntity.ok(ta.toCollectionModel(tr.findAll()));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<EntityModel<Trip>> getTrip(@PathVariable("tripId") String tripId) {
        return Optional.of(tr.findById(tripId))
                .filter(Optional::isPresent)
                .map(t -> ResponseEntity.ok(ta.toModel(t.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all trips from a certain time.
     * @return Trips from time wanted.
     */
    @GetMapping("/{cityA}/{cityB}")
    public ResponseEntity<List<Trip>> getTripsFromTime(
            @PathVariable("cityA") String cityA,
            @PathVariable("cityB") String cityB,
            @RequestParam(required = false) String type,
            @RequestParam String date) {
        try {
            LocalDateTime.parse(date);
        } catch (DateTimeException e) {
            throw new APIException(400, "La date de départ doit être au format yyyy-DD-mmTHH:mm et être valide. Exemple : '2022-03-25T11:30'");
        }

        List<Trip> trips = tr.findAllByDepartureCityAndArrivalCity(cityA.toUpperCase(), cityB.toUpperCase());
        if (trips == null || trips.isEmpty()) {
            throw new APIException(404, String.format("Il n'existe aucun trajet de %s vers %s.", cityA, cityB));
        }

        final LocalDateTime localDateTime = LocalDateTime.parse(date);
        List<Trip> filteredTrips = trips.stream().filter(t -> {
            var tripTime = t.getDepartureTime();
            return tripTime.isAfter(localDateTime);
        }).sorted(Comparator.comparing(Trip::getDepartureTime)).toList();

        if (filteredTrips.isEmpty()) {
            throw new APIException(
                    400,
                    String.format(
                            "Il n'existe aucun trajet de %s vers %s à partir du %s.",
                            cityA,
                            cityB,
                            localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss"))));
        }

        return ResponseEntity.ok(filteredTrips);
    }
}
