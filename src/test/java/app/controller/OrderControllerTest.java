package app.controller;

import app.factory.OrderResponseFactory;
import app.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    OrderService orderService;

    @InjectMocks
    OrderController orderController;

    @Captor
    ArgumentCaptor<Long> customerIdArgumentCaptor;

    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    @Nested
    class ListOrders {

        @Test
        void shouldReturnHttpOk() {

            Long customerId = 1L;
            Integer page = 0;
            Integer pageSize = 10;

            doReturn(OrderResponseFactory.builWithOneItem())
                    .when(orderService)
                    .findAll(anyLong(), any());

            doReturn(BigDecimal.valueOf(20.50))
                    .when(orderService).findTotalOnOrdersByCustomerId(anyLong());

            var response = orderController.listOrders(customerId, page, pageSize);
            assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        }

        @Test
        void shouldPassCorrectParamsToService() {

            Long customerId = 1L;
            Integer page = 0;
            Integer pageSize = 10;

            doReturn(OrderResponseFactory.builWithOneItem())
                    .when(orderService)
                    .findAll(customerIdArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());

            doReturn(BigDecimal.valueOf(20.50))
                    .when(orderService).findTotalOnOrdersByCustomerId(customerIdArgumentCaptor.capture());

            var response = orderController.listOrders(customerId, page, pageSize);

            assertEquals(2, customerIdArgumentCaptor.getAllValues().size());
            assertEquals(customerId, customerIdArgumentCaptor.getAllValues().get(0));
            assertEquals(customerId, customerIdArgumentCaptor.getAllValues().get(1));

            assertEquals(page, pageRequestArgumentCaptor.getValue().getPageNumber());
            assertEquals(pageSize, pageRequestArgumentCaptor.getValue().getPageSize());
        }

        @Test
        void shouldReturnResponseBodyCorrect() {

            Long customerId = 1L;
            Integer page = 0;
            Integer pageSize = 10;
            BigDecimal totalOnOrders = BigDecimal.valueOf(20.50);
            var pagination = OrderResponseFactory.builWithOneItem();

            doReturn(pagination)
                    .when(orderService)
                    .findAll(anyLong(), any());

            doReturn(BigDecimal.valueOf(20.50))
                    .when(orderService).findTotalOnOrdersByCustomerId(anyLong());

            var response = orderController.listOrders(customerId, page, pageSize);

            assertNotNull(response);
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertNotNull(response.getBody().pagination());
            assertNotNull(response.getBody().summary());

            assertEquals(totalOnOrders, response.getBody().summary().get("totalOnOrders"));
            assertEquals(pagination.getTotalElements(), response.getBody().pagination().totalElements());
            assertEquals(pagination.getNumber(), response.getBody().pagination().page());
            assertEquals(pagination.getSize(), response.getBody().pagination().pageSize());

            assertEquals(pagination.getContent(), response.getBody().data());
        }
    }
}