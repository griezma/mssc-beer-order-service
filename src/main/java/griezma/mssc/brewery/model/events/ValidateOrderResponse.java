package griezma.mssc.brewery.model.events;

import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data @Builder @AllArgsConstructor @ToString
public class ValidateOrderResponse {
    private BeerOrderDto order;
    private boolean valid;
}
