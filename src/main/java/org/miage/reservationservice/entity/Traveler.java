package org.miage.reservationservice.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Traveler implements Serializable {

    @Serial
    private static final long serialVersionUID = 575475475637633L;

    @Id
    @Column(name = "traveler_id")
    private String travelerId;
    private String name;

    @OneToMany(mappedBy = "traveler", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reservation> reservations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Traveler traveler = (Traveler) o;
        return travelerId != null && Objects.equals(travelerId, traveler.travelerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
