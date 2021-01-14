package griezma.mssc.beerorder.sm.actions;

import griezma.mssc.beerorder.sm.events.OrderEvent;
import griezma.mssc.brewery.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationDeniedAction implements Action<OrderStatus, OrderEvent> {
    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> stateContext) {
        UUID orderId = stateContext.getStateMachine().getUuid();
        log.debug("validation error with order id {}", orderId);
    }
}
