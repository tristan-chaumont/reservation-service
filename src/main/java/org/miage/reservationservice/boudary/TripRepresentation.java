package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Trip;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/trips", produces = MediaType.APPLICATION_JSON_VALUE)
public class TripRepresentation {

    private final TripResource tr;

    public TripRepresentation(TripResource tr) { this.tr = tr; }

    @GetMapping
    public ResponseEntity<List<Trip>> getRides() {
        return ResponseEntity.ok(tr.findAll());
    }
}
