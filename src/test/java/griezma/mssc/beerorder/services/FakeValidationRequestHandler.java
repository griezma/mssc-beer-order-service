package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.config.JmsConfig;
import griezma.mssc.brewery.model.BeerOrderDto;
import griezma.mssc.brewery.model.events.ValidateOrderRequest;
import griezma.mssc.brewery.model.events.ValidateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component @RequiredArgsConstructor
public class FakeValidationRequestHandler {
    private final JmsTemplate jms;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    void validateOrderFake(ValidateOrderRequest validateOrderRequest) {
        log.debug("validateOrderFake: " + validateOrderRequest);
        BeerOrderDto order =  validateOrderRequest.getOrder();
        jms.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE, ValidateOrderResponse.builder()
                .order(validateOrderRequest.getOrder())
                .valid(true)
                .build()
        );
    }
}
