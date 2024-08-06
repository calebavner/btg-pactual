package app.dto;

import java.util.List;

public record OrderCreatedEvent(
        Long codigoProduto,
        Long codigoCliente,
        List<OrderItemEvent> itens
) {
}
