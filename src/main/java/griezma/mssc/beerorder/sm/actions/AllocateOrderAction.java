package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.entities.BeerOrder;
import griezma.mssc.beerorder.repositories.BeerOrderRepository;
import griezma.mssc.beerorder.services.BeerOrderFlow;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.AllocateOrderRequest;
import griezma.mssc.brewery.model.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<OrderStatus, OrderEvent> {
    private final JmsTemplate jms;
    private final BeerOrderRepository repo;
    private final BeerOrderMapper dtoMapper;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        String orderId = stateContext.getMessageHeader(BeerOrderFlow.BEERORDER_ID_HEADER).toString();
        BeerOrder order = repo.findById(UUID.fromString(orderId)).orElseThrow();
        jms.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, new AllocateOrderRequest(dtoMapper.beerOrderToDto(order)));
    }
}
