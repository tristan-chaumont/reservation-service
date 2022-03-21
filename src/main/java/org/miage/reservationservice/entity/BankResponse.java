package org.miage.reservationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {

    private boolean paymentAuthorized;

    private String username;

    int port;
}
