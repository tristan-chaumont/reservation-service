package org.miage.reservationservice.control;

import org.miage.reservationservice.boudary.ReservationRepresentation;
import org.miage.reservationservice.boudary.TripRepresentation;
import org.miage.reservationservice.entity.Trip;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TripAssembler implements RepresentationModelAssembler<Trip, EntityModel<Trip>> {

    @Override
    public EntityModel<Trip> toModel(Trip trip) {
        return EntityModel.of(trip,
                linkTo(methodOn(TripRepresentation.class).getTrip(trip.getTripId())).withSelfRel(),
                linkTo(methodOn(ReservationRepresentation.class).saveReservation(null)).withRel("book"),
                linkTo(methodOn(TripRepresentation.class).getTrips()).withRel("collection"));
    }

    @Override
    public CollectionModel<EntityModel<Trip>> toCollectionModel(Iterable<? extends Trip> entities) {
        var tripModel = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .toList();
        return CollectionModel.of(tripModel,
                linkTo(methodOn(TripRepresentation.class).getTrips()).withSelfRel());
    }
}
