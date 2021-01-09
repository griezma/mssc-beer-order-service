package griezma.mssc.beerorder.config;

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
    public static final String VALIDATE_BEERORDER_QUEUE = "validate-order";
    public static final String VALIDATE_BEERORDER_RESPONSE_QUEUE = "validate-order-response";
    public static final String ALLOCATE_BEERORDER_QUEUE = "allocate-order";

    @Bean
    MessageConverter jsonConverter(ObjectMapper om) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_JsonType");
        converter.setObjectMapper(om);
        return converter;
    }
}
