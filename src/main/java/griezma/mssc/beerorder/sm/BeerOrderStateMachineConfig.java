package griezma.mssc.beerorder.sm;

import griezma.mssc.beerorder.sm.actions.AllocateOrderAction;
import griezma.mssc.brewery.model.events.OrderEvent;
import griezma.mssc.brewery.model.OrderStatus;
import griezma.mssc.beerorder.sm.actions.ValidateOrderAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    private final ValidateOrderAction validateOrder;
    private final AllocateOrderAction allocateOrder;

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderStatus.NEW)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.PICKED_UP)
                .end(OrderStatus.DELIVERED)
                .end(OrderStatus.DELIVERY_ERROR)
                .end(OrderStatus.VALIDATION_ERROR)
                .end(OrderStatus.ALLOCATION_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions.withExternal()
                    .source(OrderStatus.NEW)
                    .target(OrderStatus.VALIDATION_PENDING)
                    .event(OrderEvent.VALIDATE_ORDER)
                    .action(validateOrder)
                .and().withExternal()
                    .source(OrderStatus.VALIDATION_PENDING)
                    .target(OrderStatus.VALIDATED)
                    .event(OrderEvent.VALIDATION_PASSED)
                .and().withExternal()
                    .source(OrderStatus.VALIDATION_PENDING)
                    .target(OrderStatus.VALIDATION_ERROR)
                    .event(OrderEvent.VALIDATION_FAILED)
                .and().withExternal()
                    .source(OrderStatus.VALIDATED)
                    .target(OrderStatus.ALLOCATION_PENDING)
                    .event(OrderEvent.ALLOCATE_ORDER)
                    .action(allocateOrder)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.ALLOCATED)
                    .event(OrderEvent.ALLOCATION_SUCCESS)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.ALLOCATION_ERROR)
                    .event(OrderEvent.ALLOCATION_FAILED)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.INVENTORY_PENDING)
                    .event(OrderEvent.ALLOCATION_NO_INVENTORY);
    }
}
