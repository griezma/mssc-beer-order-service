package griezma.mssc.brewery.model.events;

import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ValidateOrderRequest {
    private BeerOrderDto order;
}
