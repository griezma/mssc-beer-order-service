package griezma.mssc.brewery.model.events;

import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder @AllArgsConstructor
public class ValidateOrderResponse {
    private BeerOrderDto order;
    private boolean valid;
}
