package app.dto;

import app.entity.OrderEntity;

import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        Long customerId,
        BigDecimal total) {

    public static OrderResponse fromEntity(OrderEntity e) {
        return new OrderResponse(e.getOrderId(), e.getCustomerId(), e.getTotal());
    }
}
