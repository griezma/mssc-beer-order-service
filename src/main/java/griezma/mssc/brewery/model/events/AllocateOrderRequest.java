package griezma.mssc.brewery.model.events;

import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AllocateOrderRequest {
    private final BeerOrderDto order;
}
