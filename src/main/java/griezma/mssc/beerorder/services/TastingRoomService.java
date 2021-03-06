package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.data.Customer;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.data.CustomerRepository;
import griezma.mssc.beerorder.bootstrap.BeerOrderBootstrap;
import griezma.mssc.brewery.model.BeerOrderDto;
import griezma.mssc.brewery.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;
    private final BeerOrderRepository beerOrderRepository;
    private final List<String> beerUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService,
                              BeerOrderRepository beerOrderRepository) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.beerOrderRepository = beerOrderRepository;

        beerUpcs.add(BeerOrderBootstrap.BEER_1_UPC);
        beerUpcs.add(BeerOrderBootstrap.BEER_2_UPC);
        beerUpcs.add(BeerOrderBootstrap.BEER_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByNameLike(BeerOrderBootstrap.TASTING_ROOM);

        if (!customerList.isEmpty()) {
            int customerIndex = (int)(System.currentTimeMillis() % customerList.size());
            doPlaceOrder(customerList.get(customerIndex));
        } else {
            log.error("No customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
//        log.debug("doPlaceOrder: customer={}", customer.getId());
        String beerToOrder = getRandomBeerUpc();

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder()
                .upc(beerToOrder)
                .orderQuantity(new Random().nextInt(6)) //todo externalize value to property
                .build();

        List<BeerOrderLineDto> beerOrderLines = new ArrayList<>();
        beerOrderLines.add(beerOrderLine);

        BeerOrderDto beerOrder = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .orderLines(beerOrderLines)
                .build();

        BeerOrderDto savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder);
    }

    private String getRandomBeerUpc() {
        return beerUpcs.get(new Random().nextInt(beerUpcs.size() -0));
    }
}
