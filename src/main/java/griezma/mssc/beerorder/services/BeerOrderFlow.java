package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderLine;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.sm.StatePersistInterceptor;
import griezma.mssc.beerorder.sm.events.OrderEvent;
import griezma.mssc.brewery.model.BeerOrderDto;
import griezma.mssc.brewery.model.BeerOrderLineDto;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.AllocateOrderResponse;
import griezma.mssc.brewery.model.events.DeallocateOrderResponse;
import griezma.mssc.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderFlow {
    private final StateMachineFactory<OrderStatus, OrderEvent> smFactory;
    private final BeerOrderRepository repo;
    private final StatePersistInterceptor statePersistInterceptor;
    private final TaskScheduler scheduler;

    @Transactional
    public BeerOrder newBeerOrder(BeerOrder order) {
        order.setId(null);
        order.setOrderStatus(OrderStatus.NEW);
        BeerOrder savedOrder = repo.save(order);
        log.debug("New beer order stored: " + savedOrder.getId());
        sendOrderEvent(savedOrder, OrderEvent.VALIDATE_ORDER);
        return savedOrder;
    }


    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    void handleValidateOrderResponse(ValidateOrderResponse response) {
        UUID orderId = response.getOrder().getId();
        if (response.isValid()) {
            validationPassed(orderId);
        } else {
            validationFailed(orderId);
        }
        log.debug("Order validation complete: {}", orderId);
    }

    @Transactional
    public void validationPassed(UUID orderId) {
        log.debug("validationPassed: {}", orderId);
        BeerOrder order = repo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Beer order not found by id: " + orderId));
        sendOrderEvent(order, OrderEvent.VALIDATION_PASSED);
        order = repo.findById(orderId).orElseThrow();
        sendOrderEvent(order, OrderEvent.ALLOCATE_ORDER);
    }

    @Transactional
    public void validationFailed(UUID orderId) {
        log.debug("validationFailed: {}", orderId);
        BeerOrder order = repo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Beer order not found by id: " + orderId));
        sendOrderEvent(order, OrderEvent.VALIDATION_FAILED);
    }

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    void handleAllocateOrderResponse(AllocateOrderResponse response) {
        BeerOrder order = repo.findById(response.getOrder().getId()).orElseThrow();
        if (response.isOrderFilled()) {
            orderAllocationPassed(order, response.getOrder());
        } else if (response.isAllocationError()) {
            orderAllocationError(order, response.getOrder());
        } else {
            orderAllocationPartiallyFilled(order, response.getOrder());
        }
    }

    public void orderAllocationPassed(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_SUCCESS);
        updateAllocation(orderDto, true);
    }

    public void orderAllocationError(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_FAILED);
    }

    public void orderAllocationPartiallyFilled(BeerOrder order, BeerOrderDto orderDto) {
        sendOrderEvent(order, OrderEvent.ALLOCATION_NO_INVENTORY);
        updateAllocation(orderDto, false);
    }

    public void pickupOrder(BeerOrder order) {
        sendOrderEvent(order, OrderEvent.ORDER_PICKED_UP);
    }

    public void cancelOrder(BeerOrder order) {
        sendOrderEvent(order, OrderEvent.CANCEL_ORDER);
    }

    @JmsListener(destination = JmsConfig.DEALLOCATE_ORDER_REQPONSE_QUEUE)
    void handleDeallocateOrderRepsonse(DeallocateOrderResponse response) {
        BeerOrder order = repo.findById(response.getOrderId()).orElseThrow();
        if (response.isComplete()) {
            sendOrderEvent(order, OrderEvent.CANCEL_SUCCESS);
        } else {
            sendOrderEvent(order, OrderEvent.CANCEL_ERROR);
        }
    }

    private void updateAllocation(BeerOrderDto orderDto, boolean filled) {
        BeerOrder order = repo.findById(orderDto.getId()).orElseThrow();
        for (BeerOrderLine orderLine : order.getOrderLines()) {
            orderDto.getOrderLines().stream()
                    .filter(dtoLine -> dtoLine.getId() == orderLine.getId())
                    .findFirst()
                    .map(BeerOrderLineDto::getAllocatedQuantity)
                    .ifPresent(orderLine::setAllocatedQuantity);
        }
        repo.save(order);
    }

    private void sendOrderEvent(BeerOrder order, OrderEvent event) {
        scheduler.schedule(() -> stateMachine(order).sendEvent(event), Instant.now().plusMillis(100));
    }

    private StateMachine<OrderStatus, OrderEvent> stateMachine(BeerOrder order) {
        // sm uuid is order id
        var sm = smFactory.getStateMachine(order.getId());
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(statePersistInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(order.getOrderStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
