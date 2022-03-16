package org.miage.reservationservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Trip implements Serializable {

    @Serial
    private static final long serialVersionUID = 1268474087482678L;

    @Id
    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "departure_city")
    private String departureCity;
    @Column(name = "arrival_city")
    private String arrivalCity;
    @Column(name = "departure_time")
    private LocalDateTime departureTime;
    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;
    @Column(name = "price")
    private double price;
    @Column(name = "num_corridor")
    private int numCorridor;
    @Column(name = "num_window")
    private int numWindow;

    @JsonBackReference
    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reservation> reservations;

    public Trip(String tripId, String departureCity, String arrivalCity, LocalDateTime departureTime, LocalDateTime arrivalTime, double price, int numCorridor, int numWindow) {
        this.tripId = tripId;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.numCorridor = numCorridor;
        this.numWindow = numWindow;
    }

    public void decrementSeat(boolean isWindowSeat) {
        if (isWindowSeat) numWindow--;
        else numCorridor--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Trip trip = (Trip) o;
        return tripId != null && Objects.equals(tripId, trip.tripId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
