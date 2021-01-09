package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.entity.BeerOrder;
import griezma.mssc.beerorder.repositories.BeerOrderRepository;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.events.OrderEvent;
import griezma.mssc.brewery.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static griezma.mssc.beerorder.services.BeerOrderProcess.BEERORDER_ID_HEADER;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<OrderStatus, OrderEvent> {
    private final JmsTemplate jms;
    private final BeerOrderRepository repo;
    private final BeerOrderMapper mapper;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        String sOrderId = stateContext.getMessageHeader(BEERORDER_ID_HEADER).toString();
        log.debug("execute order {}", sOrderId);
        BeerOrder order = repo.getOne(UUID.fromString(sOrderId));
        jms.convertAndSend("validate-order", mapper.beerOrderToDto(order));
    }
}
