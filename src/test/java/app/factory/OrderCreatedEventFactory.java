package app.factory;

import app.dto.OrderCreatedEvent;
import app.dto.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreatedEventFactory {

    public static OrderCreatedEvent build() {

        var itens = new OrderItemEvent("notebook", 1, BigDecimal.valueOf(20.50));
        var event = new OrderCreatedEvent(1L, 2L, List.of(itens));

        return event;
    }

    public static OrderCreatedEvent buildWithTwoItens() {

        var item1 = new OrderItemEvent("notebook", 1, BigDecimal.valueOf(20.50));
        var item2 = new OrderItemEvent("mouse", 1, BigDecimal.valueOf(30.50));

        var event = new OrderCreatedEvent(1L, 2L, List.of(item1, item2));

        return event;
    }
}
