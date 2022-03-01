package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationResource extends JpaRepository<Reservation, String> {

}
