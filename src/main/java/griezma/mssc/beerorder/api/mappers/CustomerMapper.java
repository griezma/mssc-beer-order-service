package griezma.mssc.beerorder.api.mappers;

import griezma.mssc.beerorder.data.Customer;
import griezma.mssc.brewery.model.CustomerDto;
import org.mapstruct.Mapper;

@Mapper(uses = DateMapper.class)
public interface CustomerMapper {
    CustomerDto customerToDto(Customer customer);
    Customer dtoToCustomer(CustomerDto dto);
}
