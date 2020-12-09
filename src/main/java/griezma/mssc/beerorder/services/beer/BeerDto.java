package griezma.mssc.beerorder.services.beer;

import lombok.Data;

import java.util.UUID;

@Data
public class BeerDto {
    private UUID id;
    private String beerName;
    private String beerStyle;
}
