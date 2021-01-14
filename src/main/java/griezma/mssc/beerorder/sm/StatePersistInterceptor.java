package griezma.mssc.beerorder.sm;

import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.BeerOrderRepository;
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
        UUID orderId = stateMachine.getUuid();
        BeerOrder order = repo.findById(orderId).orElseThrow();
        order.setOrderStatus(state.getId());
    }
}
