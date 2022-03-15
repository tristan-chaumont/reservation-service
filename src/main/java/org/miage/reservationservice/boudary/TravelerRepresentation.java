package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Traveler;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/travelers", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Traveler.class)
public class TravelerRepresentation {

    private final TravelerResource tr;

    public TravelerRepresentation(TravelerResource tr) {
        this.tr = tr;
    }

    @GetMapping
    public ResponseEntity<List<Traveler>> getTravelers() {
        return ResponseEntity.ok(tr.findAll());
    }
}
