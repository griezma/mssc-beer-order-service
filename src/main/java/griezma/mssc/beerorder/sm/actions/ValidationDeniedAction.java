package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.services.BeerOrderFlow;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.beerorder.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationDeniedAction implements Action<OrderStatus, OrderEvent> {
    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        String orderId = stateContext.getMessageHeader(BeerOrderFlow.BEERORDER_ID_HEADER).toString();
        log.debug("validation error with order id {}", stateContext.getStateMachine());
    }
}
