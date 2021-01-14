package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.events.OrderEvent;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.AllocateOrderRequest;
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
public class AllocateOrderAction implements Action<OrderStatus, OrderEvent> {
    private final JmsTemplate jms;
    private final BeerOrderRepository repo;
    private final BeerOrderMapper dtoMapper;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        UUID orderId = stateContext.getStateMachine().getUuid();
        BeerOrder order = repo.findById(orderId).orElseThrow();
        jms.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, new AllocateOrderRequest(dtoMapper.beerOrderToDto(order)));
    }
}
