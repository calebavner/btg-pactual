package app.service;

import app.dto.OrderCreatedEvent;
import app.entity.OrderEntity;
import app.factory.OrderCreatedEventFactory;
import app.factory.OrderEntityFactory;
import app.repo.OrderRepository;
import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    OrderService orderService;

    @Captor
    ArgumentCaptor<OrderEntity> orderCaptor;

    @Captor
    ArgumentCaptor<Aggregation> aggregationCaptor;

    @Nested
    class Save {

        @Test
        void shouldCallRepositorySave() {

            var event = OrderCreatedEventFactory.build();
            orderService.save(event);

            verify(orderRepository, times(1)).save(any());
        }

        @Test
        void shouldMapEventToEntityWithSuccess() {

            var event = OrderCreatedEventFactory.build();
            orderService.save(event);

            verify(orderRepository, times(1)).save(orderCaptor.capture());

            var entity = orderCaptor.getValue();

            assertEquals(event.codigoPedido(), entity.getOrderId());
            assertEquals(event.codigoCliente(), entity.getCustomerId());
            assertNotNull(entity.getTotal());
            assertEquals(event.itens().get(0).produto(), entity.getItens().get(0).getProduct());
            assertEquals(event.itens().get(0).quantidade(), entity.getItens().get(0).getQuantity());
            assertEquals(event.itens().get(0).preco(), entity.getItens().get(0).getPrice());
        }

        @Test
        void shouldCalculateOrderTotalWithSuccess() {

            OrderCreatedEvent event = OrderCreatedEventFactory.buildWithTwoItens();

            BigDecimal totalItem_1 = event.itens().get(0).preco()
                    .multiply(BigDecimal.valueOf(event.itens().get(0).quantidade()));

            BigDecimal totalItem_2 = event.itens().get(1).preco()
                    .multiply(BigDecimal.valueOf(event.itens().get(1).quantidade()));

            BigDecimal orderTotal = totalItem_1.add(totalItem_2);

            orderService.save(event);

            verify(orderRepository, times(1)).save(orderCaptor.capture());

            OrderEntity entity = orderCaptor.getValue();

            assertNotNull(entity.getTotal());
            assertEquals(orderTotal, entity.getTotal());
        }
    }

    @Nested
    class FindAllByCustomerId {

        @Test
        void shouldCallRepository() {

            Long customerId = 1L;
            PageRequest pageRequest = PageRequest.of(0, 10);

            doReturn(OrderEntityFactory.buildWithPage())
                    .when(orderRepository).findAllByCustomerId(eq(customerId), eq(pageRequest));

            var response = orderService.findAll(customerId, pageRequest);

            verify(orderRepository, times(1))
                    .findAllByCustomerId(eq(customerId), eq(pageRequest));
        }

        @Test
        void shouldMapResponse() {

            Long customerId = 1L;
            PageRequest pageRequest = PageRequest.of(0, 10);
            var page = OrderEntityFactory.buildWithPage();

            doReturn(page).when(orderRepository).findAllByCustomerId(anyLong(), any());

            var response = orderService.findAll(customerId, pageRequest);

            assertEquals(page.getTotalPages(), response.getTotalPages());
            assertEquals(page.getTotalElements(), response.getTotalElements());
            assertEquals(page.getSize(), response.getSize());
            assertEquals(page.getNumber(), response.getNumber());

            assertEquals(page.getContent().get(0).getOrderId(), response.getContent().get(0).orderId());
            assertEquals(page.getContent().get(0).getCustomerId(), response.getContent().get(0).customerId());
            assertEquals(page.getContent().get(0).getTotal(), response.getContent().get(0).total());
        }

        @Nested
        class FindTotalOnOrdersByCustomerId {

            @Test
            void shouldCallMongoTemplate() {

                Long customerId = 1L;
                var aggregationResult = mock(AggregationResults.class);
                var totalExpeted = BigDecimal.valueOf(1);

                doReturn(new Document("total", totalExpeted))
                        .when(aggregationResult).getUniqueMappedResult();

                doReturn(aggregationResult)
                        .when(mongoTemplate).aggregate(any(Aggregation.class), anyString(), eq(Document.class));

                var total = orderService.findTotalOnOrdersByCustomerId(customerId);

                verify(mongoTemplate, times(1))
                        .aggregate(any(Aggregation.class), anyString(), eq(Document.class));

                assertEquals(total, totalExpeted);
            }

            @Test
            void shouldUseCorrectAggregation() {

                Long customerId = 1L;
                var aggregationResult = mock(AggregationResults.class);
                var totalExpeted = BigDecimal.valueOf(1);

                doReturn(new Document("total", totalExpeted))
                        .when(aggregationResult).getUniqueMappedResult();

                doReturn(aggregationResult)
                        .when(mongoTemplate).aggregate(aggregationCaptor.capture(), anyString(), eq(Document.class));

                orderService.findTotalOnOrdersByCustomerId(customerId);

                var aggregation = aggregationCaptor.getValue();
                var aggregationExpeted = newAggregation(
                        match(Criteria.where("customerId").is(customerId)),
                        group().sum("total").as("total")
                );

                assertEquals(aggregationExpeted.toString(), aggregation.toString());
            }

            @Test
            void shouldQueryCorrectTable() {

                Long customerId = 1L;
                var aggregationResult = mock(AggregationResults.class);
                var totalExpeted = BigDecimal.valueOf(1);

                doReturn(new Document("total", totalExpeted))
                        .when(aggregationResult).getUniqueMappedResult();

                doReturn(aggregationResult)
                        .when(mongoTemplate).aggregate(any(Aggregation.class), eq("tb_orders"), eq(Document.class));

                orderService.findTotalOnOrdersByCustomerId(customerId);

                verify(mongoTemplate, times(1))
                        .aggregate(any(Aggregation.class), eq("tb_orders"), eq(Document.class));
            }
        }
    }
}