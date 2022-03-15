package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Reservation;
import org.miage.reservationservice.entity.Traveler;
import org.miage.reservationservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationResource extends JpaRepository<Reservation, String> {

    Optional<Reservation> findByTravelerAndTrip(Traveler traveler, Trip trip);
}
