package org.miage.reservationservice.boudary;

import org.miage.reservationservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripResource extends JpaRepository<Trip, String> {

    boolean existsByDepartureCity(String departureCity);

    boolean existsByDepartureCityAndArrivalCity(String departureCity, String arrivalCity);

    List<Trip> findAllByDepartureCityAndArrivalCity(String departureCity, String arrivalCity);
}
