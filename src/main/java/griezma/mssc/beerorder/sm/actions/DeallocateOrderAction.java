package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.brewery.model.events.DeallocateOrderRequest;
import griezma.mssc.beerorder.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static griezma.mssc.beerorder.services.BeerOrderFlow.BEERORDER_ID_HEADER;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<OrderStatus, OrderEvent> {
    private final JmsTemplate jms;
    private final BeerOrderRepository repo;
    private final BeerOrderMapper dto;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        String orderId = stateContext.getMessageHeader(BEERORDER_ID_HEADER).toString();
        BeerOrder order = repo.findById(UUID.fromString(orderId)).orElseThrow();
        jms.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE, new DeallocateOrderRequest(dto.beerOrderToDto(order)));
    }
}
