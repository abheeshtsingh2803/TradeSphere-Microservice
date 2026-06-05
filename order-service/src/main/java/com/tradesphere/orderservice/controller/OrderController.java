package com.tradesphere.orderservice.controller;

import com.tradesphere.orderservice.dto.OrderRequest;
import com.tradesphere.orderservice.service.OrderService;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place-order")
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public Mono<String> placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request)
                .thenReturn("Order placed successfully");
    }

    public Mono<String> fallbackMethod(
            OrderRequest request,
            Throwable throwable) {

        return Mono.just(
                "Oops!!! Something went wrong while placing the order. Please try again later."
        );
    }
}
