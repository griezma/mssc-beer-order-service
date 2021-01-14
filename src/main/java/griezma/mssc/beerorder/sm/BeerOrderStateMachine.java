package griezma.mssc.beerorder.sm;

import griezma.mssc.beerorder.sm.events.OrderEvent;
import griezma.mssc.beerorder.sm.actions.*;
import griezma.mssc.brewery.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachine extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    private final ValidateOrderAction validateOrder;
    private final AllocateOrderAction allocateOrder;
    private final ValidationDeniedAction validationDenied;
    private final AllocationErrorAction allocationErrorAction;
    private final DeallocateOrderAction deallocateOrder;

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStatus, OrderEvent> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderStatus.NEW)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.PICKED_UP)
                .end(OrderStatus.DELIVERED)
                .end(OrderStatus.DELIVERY_ERROR)
                .end(OrderStatus.VALIDATION_ERROR)
                .end(OrderStatus.ALLOCATION_ERROR)
                .end(OrderStatus.CANCELLED)
                .end(OrderStatus.CANCEL_ERROR);
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
                    .action(validationDenied)
                .and().withExternal()
                    .source(OrderStatus.VALIDATION_PENDING)
                    .target(OrderStatus.CANCEL_PENDING)
                    .event(OrderEvent.CANCEL_ORDER)
                .and().withExternal()
                    .source(OrderStatus.VALIDATED)
                    .target(OrderStatus.ALLOCATION_PENDING)
                    .event(OrderEvent.ALLOCATE_ORDER)
                    .action(allocateOrder)
                .and().withExternal()
                    .source(OrderStatus.VALIDATED)
                    .target(OrderStatus.CANCELLED)
                    .event(OrderEvent.CANCEL_ORDER)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.ALLOCATED)
                    .event(OrderEvent.ALLOCATION_SUCCESS)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.ALLOCATION_ERROR)
                    .event(OrderEvent.ALLOCATION_FAILED)
                    .action(allocationErrorAction)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.INVENTORY_PENDING)
                    .event(OrderEvent.ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATION_PENDING)
                    .target(OrderStatus.CANCELLED)
                    .event(OrderEvent.CANCEL_ORDER)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATED)
                    .target(OrderStatus.PICKED_UP)
                    .event(OrderEvent.ORDER_PICKED_UP)
                .and().withExternal()
                    .source(OrderStatus.ALLOCATED)
                    .target(OrderStatus.CANCEL_PENDING)
                    .event(OrderEvent.CANCEL_ORDER)
                    .action(deallocateOrder)
                .and().withExternal()
                    .source(OrderStatus.CANCEL_PENDING)
                    .target(OrderStatus.CANCELLED)
                    .event(OrderEvent.CANCEL_SUCCESS)
                .and().withExternal()
                    .source(OrderStatus.CANCEL_PENDING)
                    .target(OrderStatus.CANCEL_ERROR)
                    .event(OrderEvent.CANCEL_ERROR);
    }
}
