package griezma.mssc.beerorder.web.mappers;

import griezma.mssc.beerorder.domain.BeerOrder;
import griezma.mssc.beerorder.web.model.BeerOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

abstract class BeerOrderMapperDecorator implements BeerOrderMapper {
    @Autowired @Qualifier("delegate")
    private BeerOrderMapper delegate;

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        BeerOrderDto dto = delegate.beerOrderToDto(beerOrder);
        dto.setCustomerId(beerOrder.getCustomer().getId());
        return dto;
    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        return delegate.dtoToBeerOrder(dto);
    }
}
