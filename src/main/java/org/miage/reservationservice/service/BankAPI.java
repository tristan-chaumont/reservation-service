package org.miage.reservationservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.miage.reservationservice.entity.BankResponse;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class BankAPI {

    RestTemplate template;
    LoadBalancerClientFactory clientFactory;

    @CircuitBreaker(name = "reservation-service", fallbackMethod = "fallbackReservationCall")
    @Retry(name = "fallbackExample", fallbackMethod = "fallbackReservationCall")
    public BankResponse callBankAPI(String username, double price) {
        RoundRobinLoadBalancer lb = clientFactory.getInstance("bank-service", RoundRobinLoadBalancer.class);
        ServiceInstance instance = lb.choose().block().getServer();
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/accounts/{accountName}/debit/{amount}";
        return template.getForObject(
                url,
                BankResponse.class,
                username,
                price);
    }

    private BankResponse fallbackReservationCall(RuntimeException re) {
        return new BankResponse(false, "Unable to access service", 0);
    }
}
