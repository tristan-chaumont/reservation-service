package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripResource extends JpaRepository<Trip, String> {

}
