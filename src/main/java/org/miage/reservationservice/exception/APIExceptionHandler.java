package org.miage.reservationservice.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler({APIException.class})
    @ResponseBody()
    public ErrorResponse handleAPIException(APIException e, HttpServletResponse response) {
        response.setStatus(e.getStatusCode());
        return new ErrorResponse(e.getErrorMessage());
    }
}
