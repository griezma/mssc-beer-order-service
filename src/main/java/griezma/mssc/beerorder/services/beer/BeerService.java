package griezma.mssc.beerorder.services.beer;

import griezma.mssc.brewery.model.BeerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
public class BeerService {
    private static final String BEER_ID_PATH = "/api/v1/beer/";
    private static final String BEER_UPC_PATH = "/api/v1/beerupc/";

    @Value("${beerworks.beer-service-url:http://localhost:8080}")
    private String beerServiceUrl;

    private final RestTemplate rest;

    BeerService(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    public Optional<BeerDto> findBeerById(UUID beerId) {
        BeerDto beer = rest.getForObject(beerServiceUrl + BEER_ID_PATH + beerId, BeerDto.class);
        return Optional.ofNullable(beer);
    }

    public Optional<BeerDto> findBeerByUpc(String upc) {
        BeerDto beer = rest.getForObject(beerServiceUrl + BEER_UPC_PATH + upc, BeerDto.class);
        return Optional.ofNullable(beer);
    }
}
