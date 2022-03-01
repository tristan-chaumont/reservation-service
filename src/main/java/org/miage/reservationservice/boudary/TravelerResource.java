package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelerResource extends JpaRepository<Traveler, String> {

}
