package griezma.mssc.beerorder.web.mappers;


import griezma.mssc.beerorder.domain.BeerOrderLine;
import griezma.mssc.beerorder.services.beer.BeerDto;
import griezma.mssc.beerorder.services.beer.BeerService;
import griezma.mssc.beerorder.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
    @Autowired @Qualifier("delegate")
    private BeerOrderLineMapper delegate;

    @Autowired
    private BeerService beerService;

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto dto = delegate.beerOrderLineToDto(line);
        BeerDto beer = beerService.findBeerByUpc(line.getUpc()).orElse(null);
        if (beer != null) {
            dto.setBeerId(beer.getId());
            dto.setBeerName(beer.getBeerName());
        };
        return dto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return delegate.dtoToBeerOrderLine(dto);
    }
}
