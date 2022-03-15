package org.miage.reservationservice.entity;

import lombok.*;
import org.hibernate.Hibernate;
import org.miage.reservationservice.types.ReservationStatus;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Builder
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Reservation implements Serializable {

    @Serial
    private static final long serialVersionUID = 357547654753752987L;

    @Id
    @Column(name = "reservation_id")
    private String reservationId;

    @ManyToOne
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "window_seat")
    private boolean windowSeat;

    @Column(name = "status")
    private ReservationStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Reservation that = (Reservation) o;
        return reservationId != null && Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
