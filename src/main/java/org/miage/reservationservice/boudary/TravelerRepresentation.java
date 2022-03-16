package org.miage.reservationservice.boudary;

import org.miage.reservationservice.control.ReservationAssembler;
import org.miage.reservationservice.control.TravelerAssembler;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.exception.APIException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/travelers", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Traveler.class)
public class TravelerRepresentation {

    private final TravelerResource tr;
    private final TravelerAssembler ta;
    private final ReservationAssembler ra;

    public TravelerRepresentation(TravelerResource tr, TravelerAssembler ta, ReservationAssembler ra) {
        this.tr = tr;
        this.ta = ta;
        this.ra = ra;
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
}
