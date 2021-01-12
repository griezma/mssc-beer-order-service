package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.entities.BeerOrder;
import griezma.mssc.beerorder.entities.BeerOrderLine;
import griezma.mssc.beerorder.repositories.BeerOrderRepository;
import griezma.mssc.beerorder.sm.StatePersistInterceptor;
import griezma.mssc.brewery.model.BeerOrderDto;
import griezma.mssc.brewery.model.BeerOrderLineDto;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.AllocateOrderResponse;
import griezma.mssc.brewery.model.events.OrderEvent;
import griezma.mssc.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderFlow {
    public static final String BEERORDER_ID_HEADER = "beerorder-id";

    private final StateMachineFactory<OrderStatus, OrderEvent> smFactory;
    private final BeerOrderRepository repo;
    private final StatePersistInterceptor statePersisting;

    public BeerOrder newBeerOrder(BeerOrder order) {
        log.debug("newBeerOrder: " + order);
        order.setId(null);
        order.setOrderStatus(OrderStatus.NEW);
        BeerOrder savedOrder = repo.saveAndFlush(order);
        sendOrderEvent(savedOrder, OrderEvent.VALIDATE_ORDER);
        return savedOrder;
    }

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void handleValidateOrderResponse(ValidateOrderResponse response) {
        UUID orderId = response.getOrder().getId();
        BeerOrder order = repo.findById(orderId).orElseThrow();
        if (response.isValid()) {
            sendOrderEvent(order, OrderEvent.VALIDATION_PASSED);

            order = repo.findById(orderId).orElseThrow();
            sendOrderEvent(order, OrderEvent.ALLOCATE_ORDER);
        } else {
            sendOrderEvent(order, OrderEvent.VALIDATION_FAILED);
        }
    }

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void handleAllocateOrderResponse(AllocateOrderResponse response) {
        BeerOrder order = repo.findById(response.getOrder().getId()).orElseThrow();

        if (response.isOrderFilled()) {
            orderAllocationSuccess(order, response.getOrder());
        } else if (response.isAllocationError()) {
            orderAllocationError(order, response.getOrder());
        } else {
            orderAllocationPartiallyFilled(order, response.getOrder());
        }
    }

    public void orderAllocationSuccess(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_SUCCESS);
        updateAllocation(orderDto);
    }

    public void orderAllocationError(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_FAILED);
    }

    public void orderAllocationPartiallyFilled(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_NO_INVENTORY);
        updateAllocation(orderDto);
    }

    private void updateAllocation(BeerOrderDto orderDto) {
        BeerOrder order = repo.findById(orderDto.getId()).orElseThrow();
        for (BeerOrderLine orderLine : order.getOrderLines()) {
            orderDto.getOrderLines().stream()
                    .filter(dtoLine -> dtoLine.getId() == orderLine.getId())
                    .findFirst()
                    .map(BeerOrderLineDto::getAllocationQuantity)
                    .ifPresent(orderLine::setAllocationQuantity);
        }
        repo.save(order);
    }

    private void sendOrderEvent(BeerOrder order, OrderEvent event) {
        Message<OrderEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(BEERORDER_ID_HEADER, order.getId().toString())
                .build();
        stateMachine(order).sendEvent(message);
    }

    private StateMachine<OrderStatus, OrderEvent> stateMachine(BeerOrder order) {
        var sm = smFactory.getStateMachine(order.getId());
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(statePersisting);
            sma.resetStateMachine(new DefaultStateMachineContext<>(order.getOrderStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
