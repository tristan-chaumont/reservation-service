package org.miage.reservationservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class Train implements Serializable {

    @Serial
    private static final long serialVersionUID = 789797964342246567L;

    @Id
    @Column(name = "train_id")
    private String trainId;

    @JsonBackReference
    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Trip> trips;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Train train = (Train) o;
        return trainId != null && Objects.equals(trainId, train.trainId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
