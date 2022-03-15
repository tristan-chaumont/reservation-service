package org.miage.reservationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationInput {

    @NotNull
    @NotBlank
    private String travelerId;

    @NotNull
    @NotBlank
    private String tripId;

    @NotNull
    private boolean windowSeat;
}
