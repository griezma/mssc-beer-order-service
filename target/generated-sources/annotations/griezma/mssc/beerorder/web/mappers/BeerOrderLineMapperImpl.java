package griezma.mssc.beerorder.web.mappers;

import griezma.mssc.beerorder.domain.BeerOrderLine;
import griezma.mssc.beerorder.web.model.BeerOrderLineDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-12-07T12:27:10+0100",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.8 (N/A)"
)
@Component
public class BeerOrderLineMapperImpl implements BeerOrderLineMapper {

    @Autowired
    private DateMapper dateMapper;

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        if ( line == null ) {
            return null;
        }

        BeerOrderLineDto beerOrderLineDto = new BeerOrderLineDto();

        beerOrderLineDto.setId( line.getId() );
        if ( line.getVersion() != null ) {
            beerOrderLineDto.setVersion( line.getVersion().intValue() );
        }
        beerOrderLineDto.setCreatedDate( dateMapper.asOffsetDateTime( line.getCreatedDate() ) );
        beerOrderLineDto.setLastModifiedDate( dateMapper.asOffsetDateTime( line.getLastModifiedDate() ) );
        beerOrderLineDto.setBeerId( line.getBeerId() );
        beerOrderLineDto.setOrderQuantity( line.getOrderQuantity() );

        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrderLine beerOrderLine = new BeerOrderLine();

        beerOrderLine.setId( dto.getId() );
        if ( dto.getVersion() != null ) {
            beerOrderLine.setVersion( dto.getVersion().longValue() );
        }
        beerOrderLine.setCreatedDate( dateMapper.asTimestamp( dto.getCreatedDate() ) );
        beerOrderLine.setLastModifiedDate( dateMapper.asTimestamp( dto.getLastModifiedDate() ) );
        beerOrderLine.setBeerId( dto.getBeerId() );
        beerOrderLine.setOrderQuantity( dto.getOrderQuantity() );

        return beerOrderLine;
    }
}
