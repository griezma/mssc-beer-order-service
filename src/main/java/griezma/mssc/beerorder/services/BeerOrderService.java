/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package griezma.mssc.beerorder.services;

import griezma.mssc.beerorder.domain.BeerOrder;
import griezma.mssc.beerorder.domain.Customer;
import griezma.mssc.beerorder.domain.OrderStatus;
import griezma.mssc.beerorder.repositories.BeerOrderRepository;
import griezma.mssc.beerorder.repositories.CustomerRepository;
import griezma.mssc.beerorder.web.mappers.BeerOrderMapper;
import griezma.mssc.beerorder.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final ApplicationEventPublisher publisher;


    public Page<BeerOrderDto> listOrders(UUID customerId, Pageable pageable) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        return customerOptional.map(customer -> {
            Page<BeerOrder> beerOrderPage =
                    beerOrderRepository.findAllByCustomer(customer, pageable);
            return beerOrderPage.map(beerOrderMapper::beerOrderToDto);
        }).orElse(Page.empty());
    }

    @Transactional
    public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer Not Found: " + customerId));

        BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
        beerOrder.setCustomer(customer);
        beerOrder.setOrderStatus(OrderStatus.NEW);

        beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));
        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);

        log.debug("Saved Beer Order: " + beerOrder.getId());

        //todo impl
        //  publisher.publishEvent(new NewBeerOrderEvent(savedBeerOrder));

        return beerOrderMapper.beerOrderToDto(savedBeerOrder);
    }

    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    public void pickupOrder(UUID customerId, UUID orderId) {
        BeerOrder beerOrder = getOrder(customerId, orderId);
        beerOrder.setOrderStatus(OrderStatus.PICKED_UP);

        beerOrderRepository.save(beerOrder);
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId){
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

            if(beerOrderOptional.isPresent()){
                BeerOrder beerOrder = beerOrderOptional.get();

                // fall to exception if customer id's do not match - order not for customer
                if(beerOrder.getCustomer().getId().equals(customerId)){
                    return beerOrder;
                }
            }
            throw new RuntimeException("Beer Order Not Found");
        }
        throw new RuntimeException("Customer Not Found");
    }
}
