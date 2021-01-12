package griezma.mssc.beerorder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import griezma.mssc.beerorder.entities.BeerOrder;
import griezma.mssc.beerorder.entities.BeerOrderLine;
import griezma.mssc.beerorder.entities.Customer;
import griezma.mssc.beerorder.repositories.BeerOrderRepository;
import griezma.mssc.beerorder.repositories.CustomerRepository;
import griezma.mssc.brewery.model.BeerDto;
import griezma.mssc.brewery.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@AutoConfigureWireMock(port = 0)
public class BeerOrderFlowIT {
    @Autowired
    CustomerRepository customerRepo;

    @Autowired
    BeerOrderRepository orderRepo;

    Customer testCustomer;

    @Autowired
    BeerOrderFlow orderProcess;

    @Autowired
    ObjectMapper json;

    @Test
    void processOrderFromStatusNewToAllocated() throws Exception {
        stubFor(get(urlPathMatching("/api/v1/beerupc/.*"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(json.writeValueAsString(createBeerDto()))));

        BeerOrder order = orderProcess.newBeerOrder(createBeerOrder());
        assertEquals(OrderStatus.NEW, order.getOrderStatus());

        BeerOrder validationPending = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.VALIDATION_PENDING, validationPending.getOrderStatus());

        await().atMost(1, SECONDS)
                .until(() -> orderRepo.findById(order.getId()).map(BeerOrder::getOrderStatus).orElseThrow(), Predicate.isEqual(OrderStatus.ALLOCATED));
    }

    @BeforeEach
    void setup() {
        testCustomer = customerRepo.save(Customer.builder()
                .customerName("test customer")
                .build());
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