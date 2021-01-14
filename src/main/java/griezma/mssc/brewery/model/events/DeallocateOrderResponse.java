package griezma.mssc.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class DeallocateOrderResponse {
    private UUID orderId;
    private boolean complete;
}
