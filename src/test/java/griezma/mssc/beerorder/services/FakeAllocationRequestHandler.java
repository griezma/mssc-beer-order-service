package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.brewery.model.events.AllocateOrderRequest;
import griezma.mssc.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeAllocationRequestHandler {
    private final JmsTemplate jms;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    void allocationRequest(AllocateOrderRequest request) {
        log.debug("order allocation request: " + request);
        var order = request.getOrder();
        boolean allocationError = "test-allocation-error".equals(order.getCustomerRef());
        boolean orderFilled = !(allocationError || "test-allocation-partial".equals(order.getCustomerRef()));

        if (!allocationError) {
            if (orderFilled) {
                order.getOrderLines().forEach(line -> line.setAllocatedQuantity(line.getOrderQuantity()));
            } else {
                order.getOrderLines().forEach(line -> line.setAllocatedQuantity(line.getOrderQuantity() - 1));
            }
        }
        jms.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, new AllocateOrderResponse(order, orderFilled, allocationError));
    }
}
