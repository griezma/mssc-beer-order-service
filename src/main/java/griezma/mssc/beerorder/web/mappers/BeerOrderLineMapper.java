package griezma.mssc.beerorder.web.mappers;

import griezma.mssc.beerorder.domain.BeerOrderLine;
import griezma.mssc.beerorder.web.model.BeerOrderLineDto;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface BeerOrderLineMapper {
    BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

    BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
