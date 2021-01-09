package griezma.mssc.brewery.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BeerDto {
    private UUID id;
    private String beerName;
    private String beerStyle;
    private BigDecimal price;
}
