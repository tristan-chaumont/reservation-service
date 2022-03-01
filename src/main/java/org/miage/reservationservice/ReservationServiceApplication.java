package org.miage.reservationservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    /**
     * localhost:8082/swagger-ui/index.html
     */
    @Bean
    public OpenAPI reservationAPI() {
        return new OpenAPI().info(new Info()
                .title("Reservation API")
                .version("1.0")
                .description("Documentation sommaire de l'API Reservation 1.0"));
    }
}
