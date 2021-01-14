package griezma.mssc.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor @NoArgsConstructor
public class OrderAllocationFailure {
    private UUID orderId;
}
