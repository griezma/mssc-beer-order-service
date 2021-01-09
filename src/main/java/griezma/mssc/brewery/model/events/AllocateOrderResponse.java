package griezma.mssc.brewery.model.events;

import griezma.mssc.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder @AllArgsConstructor
public class AllocateOrderResponse {
    private BeerOrderDto order;
    private boolean orderFilled;
    private boolean allocationError = false;
}
