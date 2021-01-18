package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.sm.events.OrderEvent;
import griezma.mssc.beerorder.api.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.DeallocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<OrderStatus, OrderEvent> {
    private final JmsTemplate jms;
    private final BeerOrderRepository repo;
    private final BeerOrderMapper dto;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        UUID orderId = stateContext.getStateMachine().getUuid();
        BeerOrder order = repo.findById(orderId).orElseThrow();
        jms.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE, new DeallocateOrderRequest(dto.beerOrderToDto(order)));
    }
}
