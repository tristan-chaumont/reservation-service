package org.miage.reservationservice.control;

import org.miage.reservationservice.boudary.ReservationRepresentation;
import org.miage.reservationservice.boudary.TravelerRepresentation;
import org.miage.reservationservice.boudary.TripRepresentation;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TravelerAssembler implements RepresentationModelAssembler<Traveler, EntityModel<Traveler>> {

    @Override
    public EntityModel<Traveler> toModel(Traveler traveler) {
        return EntityModel.of(traveler,
                linkTo(methodOn(TravelerRepresentation.class).getOneTraveler(traveler.getTravelerId())).withSelfRel(),
                linkTo(methodOn(TravelerRepresentation.class).getTravelerReservations(traveler.getTravelerId())).withRel("myReservations"),
                linkTo(methodOn(TravelerRepresentation.class).getTravelers()).withRel("collection"));
    }

    @Override
    public CollectionModel<EntityModel<Traveler>> toCollectionModel(Iterable<? extends Traveler> entities) {
        var travelerModel = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .toList();
        return CollectionModel.of(travelerModel,
                linkTo(methodOn(TravelerRepresentation.class).getTravelers()).withSelfRel());
    }
}
