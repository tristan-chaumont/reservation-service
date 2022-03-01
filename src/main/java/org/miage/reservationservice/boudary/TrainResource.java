package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainResource extends JpaRepository<Train, String> {

}
