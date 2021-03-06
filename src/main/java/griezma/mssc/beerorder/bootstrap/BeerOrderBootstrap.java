package griezma.mssc.beerorder.bootstrap;

import griezma.mssc.beerorder.data.Customer;
import griezma.mssc.beerorder.data.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderBootstrap implements ApplicationListener<ApplicationReadyEvent> {
    public static final String TASTING_ROOM = "Tasting Room";
    public static final String BEER_1_UPC = "0631234200036";
    public static final String BEER_2_UPC = "0631234300019";
    public static final String BEER_3_UPC = "0083783375213";

    private final CustomerRepository customerRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent ready) {
        log.info("Application ready");
        if (customerRepository.count() == 0) {
            Customer customer = Customer.builder()
                    .name(TASTING_ROOM)
                    .apiKey(UUID.randomUUID())
                    .build();
            customerRepository.save(customer);
            log.info("Tasting Room customer created: {}", customer.getId());
        }
    }
}
