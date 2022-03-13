package org.miage.reservationservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIException extends RuntimeException {
    public final int statusCode;
    public final String errorMessage;
}
