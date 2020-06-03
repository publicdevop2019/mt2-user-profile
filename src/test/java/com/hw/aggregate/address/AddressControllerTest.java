package com.hw.aggregate.address;

import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.model.Address;
import com.hw.aggregate.address.representation.AddressRepresentation;
import com.hw.aggregate.address.representation.AddressSummaryRepresentation;
import com.hw.shared.JwtTokenExtractException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.hw.aggregate.Helper.*;
import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class AddressControllerTest {
    @InjectMocks
    AddressController addressController;
    @Mock
    AddressApplicationService addressApplicationService;

    @Test(expected = JwtTokenExtractException.class)
    public void getAllAddresses_wrong_jwt() {
        ResponseEntity<List<AddressSummaryRepresentation.AddressSummaryCustomerRepresentation>> allAddresses = addressController.getAllAddresses(rStr(), rLong());
        Assert.assertNotNull(allAddresses.getBody());
    }

    @Test
    public void getAllAddresses() {
        Mockito.doReturn(new AddressSummaryRepresentation(List.of(new Address()))).when(addressApplicationService).getAllAddressesForCustomer(anyString(), anyLong());
        ResponseEntity<List<AddressSummaryRepresentation.AddressSummaryCustomerRepresentation>> allAddresses = addressController.getAllAddresses(rJwt(), rLong());
        Assert.assertNotNull(allAddresses.getBody());
    }

    @Test
    public void getAddressInById() {
        Mockito.doReturn(new AddressRepresentation(new Address())).when(addressApplicationService).getAddressInById(anyString(), anyLong(), anyLong());
        ResponseEntity<AddressRepresentation> addressInById = addressController.getAddressInById(rJwt(), rLong(), rLong());
        Assert.assertNotNull(addressInById.getBody());
    }

    @Test
    public void createAddress() {
        Address address1 = new Address();
        address1.setId(100L);
        Mockito.doReturn(new AddressRepresentation(address1)).when(addressApplicationService).createAddress(anyString(), anyLong(), any(CreateAddressCommand.class));
        ResponseEntity<Void> address = addressController.createAddress(rJwt(), rLong(), new CreateAddressCommand());
        Assert.assertEquals("100", address.getHeaders().getLocation().toString());
        Mockito.verify(addressApplicationService, Mockito.times(1)).createAddress(anyString(), anyLong(), any(CreateAddressCommand.class));
    }

    @Test
    public void updateAddress() {
        ResponseEntity<Void> address = addressController.updateAddress(rJwt(), rLong(), rLong(), new UpdateAddressCommand());
        Mockito.verify(addressApplicationService, Mockito.times(1)).updateAddress(anyString(), anyLong(), anyLong(), any(UpdateAddressCommand.class));
    }

    @Test
    public void deleteAddress() {
        ResponseEntity<Void> address = addressController.deleteAddress(rJwt(), rLong(), rLong());
        Mockito.verify(addressApplicationService, Mockito.times(1)).deleteAddress(anyString(), anyLong(), anyLong());
    }

}