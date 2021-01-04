package griezma.mssc.beerorder.web.mappers;

import griezma.mssc.beerorder.domain.BeerOrder;
import griezma.mssc.beerorder.domain.BeerOrderLine;
import griezma.mssc.beerorder.domain.Customer;
import griezma.mssc.beerorder.web.model.BeerOrderDto;
import griezma.mssc.beerorder.web.model.BeerOrderLineDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-01-04T10:51:42+0100",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 11.0.8 (Ubuntu)"
)
@Component
public class BeerOrderMapperImpl implements BeerOrderMapper {

    @Autowired
    private DateMapper dateMapper;
    @Autowired
    private BeerOrderLineMapper beerOrderLineMapper;

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        if ( beerOrder == null ) {
            return null;
        }

        BeerOrderDto beerOrderDto = new BeerOrderDto();

        beerOrderDto.setCustomerId( beerOrderCustomerId( beerOrder ) );
        beerOrderDto.setId( beerOrder.getId() );
        beerOrderDto.setCreatedDate( dateMapper.asOffsetDateTime( beerOrder.getCreatedDate() ) );
        beerOrderDto.setLastModifiedDate( dateMapper.asOffsetDateTime( beerOrder.getLastModifiedDate() ) );
        beerOrderDto.setCustomerRef( beerOrder.getCustomerRef() );
        beerOrderDto.setBeerOrderLines( beerOrderLineSetToBeerOrderLineDtoList( beerOrder.getBeerOrderLines() ) );
        beerOrderDto.setOrderStatus( beerOrder.getOrderStatus() );
        beerOrderDto.setOrderStatusCallbackUrl( beerOrder.getOrderStatusCallbackUrl() );

        return beerOrderDto;
    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrder beerOrder = new BeerOrder();

        beerOrder.setId( dto.getId() );
        beerOrder.setCreatedDate( dateMapper.asTimestamp( dto.getCreatedDate() ) );
        beerOrder.setLastModifiedDate( dateMapper.asTimestamp( dto.getLastModifiedDate() ) );
        beerOrder.setCustomerRef( dto.getCustomerRef() );
        beerOrder.setBeerOrderLines( beerOrderLineDtoListToBeerOrderLineSet( dto.getBeerOrderLines() ) );
        beerOrder.setOrderStatus( dto.getOrderStatus() );
        beerOrder.setOrderStatusCallbackUrl( dto.getOrderStatusCallbackUrl() );

        return beerOrder;
    }

    private UUID beerOrderCustomerId(BeerOrder beerOrder) {
        if ( beerOrder == null ) {
            return null;
        }
        Customer customer = beerOrder.getCustomer();
        if ( customer == null ) {
            return null;
        }
        UUID id = customer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<BeerOrderLineDto> beerOrderLineSetToBeerOrderLineDtoList(Set<BeerOrderLine> set) {
        if ( set == null ) {
            return null;
        }

        List<BeerOrderLineDto> list = new ArrayList<BeerOrderLineDto>( set.size() );
        for ( BeerOrderLine beerOrderLine : set ) {
            list.add( beerOrderLineMapper.beerOrderLineToDto( beerOrderLine ) );
        }

        return list;
    }

    protected Set<BeerOrderLine> beerOrderLineDtoListToBeerOrderLineSet(List<BeerOrderLineDto> list) {
        if ( list == null ) {
            return null;
        }

        Set<BeerOrderLine> set = new HashSet<BeerOrderLine>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( BeerOrderLineDto beerOrderLineDto : list ) {
            set.add( beerOrderLineMapper.dtoToBeerOrderLine( beerOrderLineDto ) );
        }

        return set;
    }
}
