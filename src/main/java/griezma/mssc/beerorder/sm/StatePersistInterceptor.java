package griezma.mssc.beerorder.sm;

import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.services.BeerOrderFlow;
import griezma.mssc.beerorder.events.OrderEvent;
import griezma.mssc.brewery.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatePersistInterceptor extends StateMachineInterceptorAdapter<OrderStatus, OrderEvent> {
    private final BeerOrderRepository repo;

    @Override @Transactional
    public void preStateChange(State<OrderStatus, OrderEvent> state, Message<OrderEvent> message, Transition<OrderStatus, OrderEvent> transition, StateMachine<OrderStatus, OrderEvent> stateMachine) {
        if (message == null) return;
        String sOrderId = message.getHeaders().get(BeerOrderFlow.BEERORDER_ID_HEADER, String.class);
        log.debug("preStateChange: {}, order={}", state.getId(), sOrderId);
        UUID orderId = UUID.fromString(sOrderId);
        BeerOrder order = repo.getOne(orderId);
        order.setOrderStatus(state.getId());
    }
}
