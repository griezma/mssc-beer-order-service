package griezma.mssc.beerorder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderLine;
import griezma.mssc.beerorder.data.Customer;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.data.CustomerRepository;
import griezma.mssc.beerorder.sm.actions.ValidationDeniedAction;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.BeerDto;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.OrderAllocationFailure;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@AutoConfigureWireMock
public class BeerOrderFlowIT {
    @Autowired
    CustomerRepository customerRepo;

    @Autowired
    BeerOrderRepository orderRepo;

    Customer testCustomer;

    @Autowired
    BeerOrderFlow orderFlow;

    @Autowired
    ObjectMapper json;

    @Autowired
    BeerOrderMapper dtoMapper;

    @SpyBean
    ValidationDeniedAction validationDenied;

    @Autowired
    JmsTemplate jms;

    @Test
    void orderFromStatusNewToAllocated() throws Exception {
        log.debug("processOrderFromStatusNewToAllocated");

        stubFor(get(urlPathMatching("/api/v1/beerupc/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.writeValueAsString(createBeerDto()))));

        BeerOrder order = orderFlow.newBeerOrder(createBeerOrder());
        assertEquals(OrderStatus.NEW, order.getOrderStatus());

        BeerOrder validationPending = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.VALIDATION_PENDING, validationPending.getOrderStatus());

        await().atMost(1, SECONDS)
                .untilAsserted(() -> {
                    BeerOrder allocated = orderRepo.findById(order.getId()).orElseThrow();
                    assertEquals(OrderStatus.ALLOCATED, allocated.getOrderStatus());
                });

        BeerOrder allocated = orderRepo.findById(order.getId()).orElseThrow();
        allocated.getOrderLines().forEach(orderLine -> assertEquals(orderLine.getOrderQuantity(), orderLine.getAllocatedQuantity(), "full order allocation expected"));
    }

    @Test
    void orderStatusAllocatedToPickedUp() {
        BeerOrder order = createAllocatedBeerOrder();
        orderFlow.pickupOrder(order);
        await().atMost(Duration.ofMillis(500)).untilAsserted(() -> {
            BeerOrder pickedUp = orderRepo.findById(order.getId()).orElseThrow();
            assertEquals(OrderStatus.PICKED_UP, pickedUp.getOrderStatus());
        });
    }

    @Test
    void testOrderValidationFailed() throws Exception {

        stubFor(get(urlPathMatching("/api/v1/beerupc/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.writeValueAsString(createBeerDto()))));

        BeerOrder testBeerOrder = createBeerOrder();
        testBeerOrder.setCustomerRef("test-validation-error");
        BeerOrder order = orderFlow.newBeerOrder(testBeerOrder);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());

        BeerOrder validationPending = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.VALIDATION_PENDING, validationPending.getOrderStatus());

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    BeerOrder allocated = orderRepo.findById(order.getId()).orElseThrow();
                    assertEquals(OrderStatus.VALIDATION_ERROR, allocated.getOrderStatus());
                });

        Mockito.verify(validationDenied).execute(Mockito.any(StateContext.class));
    }

    @Test @Timeout(1)
    void testOrderAllocationError() throws Exception {

        stubFor(get(urlPathMatching("/api/v1/beerupc/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.writeValueAsString(createBeerDto()))));

        BeerOrder testBeerOrder = createBeerOrder();
        testBeerOrder.setCustomerRef("test-allocation-error");
        BeerOrder order = orderFlow.newBeerOrder(testBeerOrder);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());

        BeerOrder validationPending = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.VALIDATION_PENDING, validationPending.getOrderStatus());

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    BeerOrder allocated = orderRepo.findById(order.getId()).orElseThrow();
                    assertEquals(OrderStatus.ALLOCATION_ERROR, allocated.getOrderStatus());
                });

        OrderAllocationFailure allocationError = (OrderAllocationFailure) jms.receiveAndConvert(JmsConfig.ALLOCATION_ERROR_QUEUE);
        assertEquals(order.getId(), allocationError.getOrderId());
    }

    @Test
    void testOrderWithPartialAllocation() throws Exception {
        log.debug("processOrderFromStatusNewToAllocated");

        stubFor(get(urlPathMatching("/api/v1/beerupc/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.writeValueAsString(createBeerDto()))));

        BeerOrder testBeerOrder = createBeerOrder();
        testBeerOrder.setCustomerRef("test-allocation-partial");
        BeerOrder order = orderFlow.newBeerOrder(testBeerOrder);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());

        BeerOrder validationPending = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.VALIDATION_PENDING, validationPending.getOrderStatus());

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    BeerOrder allocated = orderRepo.findById(order.getId()).orElseThrow();
                    assertEquals(OrderStatus.INVENTORY_PENDING, allocated.getOrderStatus());
                });
    }

    @Test
    void cancelValidatedOrder() {
        BeerOrder validated = createValidatedBeerOrder();
        var orderId = validated.getId();
        orderFlow.cancelOrder(validated);
        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    BeerOrder cancelled = orderRepo.findById(orderId).orElseThrow();
                    assertEquals(OrderStatus.CANCELLED, cancelled.getOrderStatus());
                });
    }

    @BeforeEach
    void setup() {
        testCustomer = customerRepo.save(Customer.builder()
                .customerName("test customer")
                .build());
    }

    BeerOrder createAllocatedBeerOrder() {
        BeerOrder order = createBeerOrder();
        order.getOrderLines().forEach(line -> {
            line.setAllocatedQuantity(line.getOrderQuantity());
        });
        order.setOrderStatus(OrderStatus.ALLOCATED);
        return orderRepo.save(order);
    }

    BeerOrder createValidatedBeerOrder() {
        BeerOrder order = createBeerOrder();
        order.getOrderLines().forEach(line -> {
            line.setAllocatedQuantity(line.getOrderQuantity());
        });
        order.setOrderStatus(OrderStatus.VALIDATED);
        return orderRepo.save(order);
    }

    BeerOrder createBeerOrder() {
        Set<BeerOrderLine> orderLines = new HashSet<>(List.of(
                BeerOrderLine.builder()
                        .beerId(UUID.randomUUID())
                        .upc("12345")
                        .orderQuantity(11)
                        .build()

        ));
        return BeerOrder.builder()
                .orderStatus(OrderStatus.NEW)
                .customer(testCustomer)
                .orderLines(orderLines)
                .build();
    }

    BeerDto createBeerDto() {
        return BeerDto.builder()
                .id(UUID.randomUUID())
                .upc("12345")
                .beerName("Test Beer")
                .beerStyle("Test Ale")
                .build();
    }
}