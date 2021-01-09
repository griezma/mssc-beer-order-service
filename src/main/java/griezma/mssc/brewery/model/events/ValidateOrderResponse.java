package griezma.mssc.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder @AllArgsConstructor
public class ValidateOrderResponse {
    private UUID orderId;
    private boolean valid;
}
