package com.tradesphere.orderservice.service;

import com.tradesphere.orderservice.dto.InventoryResponse;
import com.tradesphere.orderservice.dto.OrderLineItemsDto;
import com.tradesphere.orderservice.dto.OrderRequest;
import com.tradesphere.orderservice.model.Order;
import com.tradesphere.orderservice.model.OrderLineItems;
import com.tradesphere.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    public void placeOrder(OrderRequest orderRequest) {

        if (orderRequest.getOrderLineItemsDtoList() == null ||
                orderRequest.getOrderLineItemsDtoList().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
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

        InventoryResponse[] inventoryResponses = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder
                                .queryParam("skuCode", skuCodes)
                                .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryResponses == null) {
            throw new IllegalStateException("Inventory service did not respond");
        }

        // UPDATED VALIDATION LOGIC
        boolean allProductsAvailable = orderLineItemsList.stream()
                .allMatch(orderItem -> {
                    InventoryResponse inventoryResponse = Arrays.stream(inventoryResponses)
                            .filter(inv -> inv.getSkuCode().equals(orderItem.getSkuCode()))
                            .findFirst()
                            .orElse(null);

                    if (inventoryResponse == null) {
                        throw new IllegalArgumentException("Product with SKU: " + orderItem.getSkuCode() + " not found in inventory");
                    }

                    return inventoryResponse.getQuantity() >= orderItem.getQuantity();
                });

        if (allProductsAvailable) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Insufficient quantity available for one or more products");
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
