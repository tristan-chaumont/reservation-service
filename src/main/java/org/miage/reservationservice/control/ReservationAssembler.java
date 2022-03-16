package org.miage.reservationservice.control;

import org.miage.reservationservice.boudary.ReservationRepresentation;
import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.types.ReservationStatus;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReservationAssembler implements RepresentationModelAssembler<Reservation, EntityModel<Reservation>> {

    @Override
    public EntityModel<Reservation> toModel(Reservation reservation) {
        List<Link> links = new ArrayList<>();

        links.add(linkTo(methodOn(ReservationRepresentation.class).getOneReservation(reservation.getReservationId())).withSelfRel());

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            links.add(linkTo(methodOn(ReservationRepresentation.class).cancelReservation(reservation.getReservationId())).withRel("cancel"));
            links.add(linkTo(methodOn(ReservationRepresentation.class).confirmReservation(reservation.getReservationId())).withRel("confirm"));
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            //PAY
        }

        links.add(linkTo(methodOn(ReservationRepresentation.class).getAllReservations()).withRel("collection"));

        return EntityModel.of(reservation, links);
    }

    @Override
    public CollectionModel<EntityModel<Reservation>> toCollectionModel(Iterable<? extends Reservation> entities) {
        var reservationModel = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .toList();
        return CollectionModel.of(reservationModel,
                linkTo(methodOn(ReservationRepresentation.class).getAllReservations()).withSelfRel());
    }
}
