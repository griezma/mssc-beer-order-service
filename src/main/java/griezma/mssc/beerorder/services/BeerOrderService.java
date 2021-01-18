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

import griezma.mssc.beerorder.data.BeerOrder;
import griezma.mssc.beerorder.data.Customer;
import griezma.mssc.beerorder.data.BeerOrderRepository;
import griezma.mssc.beerorder.data.CustomerRepository;
import griezma.mssc.beerorder.api.mappers.BeerOrderMapper;
import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
//    private final ApplicationEventPublisher publisher;
    private final BeerOrderFlow orderFlow;

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
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
                orderFlow.newBeerOrder(beerOrderMapper.dtoToBeerOrder(beerOrderDto));
        beerOrder.setCustomer(customer);
        beerOrder.getOrderLines().forEach(line -> line.setBeerOrder(beerOrder));

        BeerOrder savedBeerOrder = orderFlow.newBeerOrder(beerOrder);

        //todo impl
        //  publisher.publishEvent(new NewBeerOrderEvent(savedBeerOrder));

        return beerOrderMapper.beerOrderToDto(savedBeerOrder);
    }

    public void pickupOrder(UUID customerId, UUID orderId) {
        BeerOrder order = beerOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + customerId));

        orderFlow.pickupOrder(order);
    }

    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        BeerOrder beerOrder = beerOrderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Beer Order not found"));

        // fall to exception if customer ids do not match - order not for customer
        if (beerOrder.getCustomer().getId().equals(customerId)) {
            return beerOrder;
        } else {
            throw new RuntimeException("Beer Order Not Found");
        }
    }
}
