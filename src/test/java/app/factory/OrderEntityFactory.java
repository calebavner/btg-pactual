package app.factory;

import app.dto.OrderResponse;
import app.entity.OrderEntity;
import app.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;

public class OrderEntityFactory {

    public static OrderEntity build() {

        OrderItem itens = new OrderItem("notebook", 1, BigDecimal.valueOf(20.50));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(1L);
        orderEntity.setCustomerId(2L);
        orderEntity.setTotal(BigDecimal.valueOf(20.50));
        orderEntity.setItens(List.of(itens));

        return orderEntity;
    }

    public static Page<OrderEntity> buildWithPage() {
        return new PageImpl<>(List.of(build()));
    }
}
