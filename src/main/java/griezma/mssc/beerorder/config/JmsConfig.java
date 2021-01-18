package griezma.mssc.beerorder.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
@RequiredArgsConstructor
public class JmsConfig {
    public static final String VALIDATE_ORDER_QUEUE = "validate-order";
    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";
    public static final String ALLOCATE_ORDER_QUEUE = "allocate-order";
    public static final String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-reponse";
    public static final String ALLOCATION_ERROR_QUEUE = "order-allocation-error";
    public static final String DEALLOCATE_ORDER_QUEUE = "deallocate-order";
    public static final String DEALLOCATE_ORDER_REQPONSE_QUEUE = "deallocate-order-response";

    @Bean
    MessageConverter jsonConverter(ObjectMapper om) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_JsonType");
        converter.setObjectMapper(om);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return converter;
    }
}
