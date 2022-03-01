package org.miage.reservationservice.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Time;
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
    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;
    @Column(name = "departure_city")
    private String departureCity;
    @Column(name = "arrival_city")
    private String arrivalCity;
    @Column(name = "departure_time")
    private Time departureTime;
    @Column(name = "arrival_time")
    private Time arrivalTime;
    private double price;

    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reservation> reservations;

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
