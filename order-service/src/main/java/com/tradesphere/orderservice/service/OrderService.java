package com.tradesphere.orderservice.service;

import com.tradesphere.orderservice.dto.InventoryResponse;
import com.tradesphere.orderservice.dto.OrderLineItemsDto;
import com.tradesphere.orderservice.dto.OrderRequest;
import com.tradesphere.orderservice.event.OrderPlacedEvent;
import com.tradesphere.orderservice.model.Order;
import com.tradesphere.orderservice.model.OrderLineItems;
import com.tradesphere.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public Mono<Void> placeOrder(OrderRequest orderRequest) {

        if (orderRequest.getOrderLineItemsDtoList() == null ||
                orderRequest.getOrderLineItemsDtoList().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Order items cannot be null or empty"));
        }

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToEntity)
                .toList();

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodes = orderLineItemsList.stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        Span inventoryServiceLookUp = tracer.nextSpan().name("InventoryServiceLookUp");

        try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookUp.start())) {
            return webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder
                                    .queryParam("skuCode", skuCodes)
                                    .build())

                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .flatMap(inventoryResponses -> {

                        boolean allProductsAvailable = orderLineItemsList.stream()
                                .allMatch(orderItem -> {
                                    InventoryResponse inventoryResponse = Arrays.stream(inventoryResponses)
                                            .filter(inv -> inv.getSkuCode().equals(orderItem.getSkuCode()))
                                            .findFirst()
                                            .orElse(null);

                                    if (inventoryResponse == null) {
                                        throw new IllegalArgumentException(
                                                "Product with SKU: " + orderItem.getSkuCode() + " not found in inventory");
                                    }

                                    return inventoryResponse.getQuantity() >= orderItem.getQuantity();
                                });

                        if (allProductsAvailable) {
                            orderRepository.save(order); // ⚠️ blocking (see note below)
                            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                            return Mono.empty();
                        } else {
                            return Mono.error(new IllegalArgumentException(
                                    "Insufficient quantity available for one or more products"));
                        }
                    });
        } finally {
            inventoryServiceLookUp.end();
        }
    }


    private OrderLineItems mapToEntity(OrderLineItemsDto dto) {
        OrderLineItems item = new OrderLineItems();
        item.setSkuCode(dto.getSkuCode());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        return item;
    }
}
