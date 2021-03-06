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
    public ResponseEntity<CollectionModel<EntityModel<Trip>>> getTripsFromTime(
            @PathVariable("cityA") String cityA,
            @PathVariable("cityB") String cityB,
            @RequestParam(required = false) String type,
            @RequestParam String date,
            @RequestParam boolean windowSeat,
            @RequestParam(required = false) boolean sortByPrice) {
        // v??rification du type de voyage
        if (type != null && (!type.equals("aller") && !type.equals("aller-retour"))) {
            throw new APIException(400, "Le type du trajet doit ??tre 'aller' ou 'aller-retour'");
        }

        // v??rification de la date
        try {
            LocalDateTime.parse(date);
        } catch (DateTimeException e) {
            throw new APIException(400, "La date de d??part doit ??tre au format yyyy-DD-mmTHH:mm et ??tre valide. Exemple : '2022-03-25T11:30'");
        }

        // r??cup??ration des trajets de cityA vers cityB
        List<Trip> tripsAller = tr.findAllByDepartureCityAndArrivalCity(cityA.toUpperCase(), cityB.toUpperCase());
        if (tripsAller == null || tripsAller.isEmpty()) {
            throw new APIException(404, String.format("Il n'existe aucun trajet de %s vers %s.", cityA, cityB));
        }

        final LocalDateTime localDateTime = LocalDateTime.parse(date);

        // trajets aller pour le jour entr?? par l'utilisateur
        List<Trip> filteredTrips = tripsAller.stream().filter(t -> {
            var tripTime = t.getDepartureTime();
            boolean isSeatAvailable = (windowSeat && t.getNumWindow() > 0) || (!windowSeat && t.getNumCorridor() > 0);
            return tripTime.isAfter(localDateTime) && tripTime.getDayOfMonth() == localDateTime.getDayOfMonth() && isSeatAvailable;
        }).sorted(Comparator.comparing(Trip::getDepartureTime)).toList();

        if (filteredTrips.isEmpty()) {
            throw new APIException(
                    404,
                    String.format(
                            "Il n'existe aucun trajet de %s vers %s ?? partir du %s pour une place c??t?? %s.",
                            cityA,
                            cityB,
                            localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy ?? HH:mm:ss")),
                            windowSeat ? "fen??tre" : "couloir"));
        }

        // trajets retour ?? partir du lendemain et jusque 7 jours apr??s
        if (type != null && type.equals("aller-retour")) {
            // r??cup??ration des trajets de cityB vers cityA
            List<Trip> tripsRetour = tr.findAllByDepartureCityAndArrivalCity(cityB.toUpperCase(), cityA.toUpperCase());
            if (tripsRetour == null || tripsRetour.isEmpty()) {
                throw new APIException(404, String.format("Il n'existe aucun trajet retour de %s vers %s.", cityB, cityA));
            }

            List<Trip> filteredTripsRetour = tripsRetour.stream().filter(t -> {
                var tripTime = t.getDepartureTime();
                var tomorrow = localDateTime.plusDays(1);
                var maxInterval = localDateTime.plusDays(8);
                boolean isSeatAvailable = (windowSeat && t.getNumWindow() > 0) || (!windowSeat && t.getNumCorridor() > 0);
                return tripTime.isAfter(tomorrow) && tripTime.isBefore(maxInterval) && isSeatAvailable;
            }).toList();

            if (filteredTripsRetour.isEmpty()) {
                throw new APIException(
                        404,
                        String.format(
                                "Il n'existe aucun trajet retour de %s vers %s ?? partir du lendemain et sur un intervalle de 7 jours.",
                                cityB,
                                cityA));
            }
        }

        if (sortByPrice) {
            return ResponseEntity.ok(ta.toCollectionModel(filteredTrips.stream().sorted((a, b) -> (int) (a.getPrice() - b.getPrice())).toList()));
        } else {
            return ResponseEntity.ok(ta.toCollectionModel(filteredTrips));
        }
    }
}
