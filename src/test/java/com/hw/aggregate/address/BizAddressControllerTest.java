package com.hw.aggregate.address;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BizAddressControllerTest {
    @InjectMocks
    BizAddressController addressController;
//    @Mock
//    AddressApplicationService addressApplicationService;

//    @Test(expected = JwtTokenExtractException.class)
//    public void getAllAddresses_wrong_jwt() {
//        ResponseEntity<List<AddressSummaryRepresentation.AddressSummaryCustomerRepresentation>> allAddresses = addressController.getAllAddresses(rStr(), rLong());
//        Assert.assertNotNull(allAddresses.getBody());
//    }
//
//    @Test
//    public void getAllAddresses() {
//        Mockito.doReturn(new AddressSummaryRepresentation(List.of(new BizAddress()))).when(addressApplicationService).getAllAddressesForCustomer(anyString(), anyLong());
//        ResponseEntity<List<AddressSummaryRepresentation.AddressSummaryCustomerRepresentation>> allAddresses = addressController.getAllAddresses(rJwt(), rLong());
//        Assert.assertNotNull(allAddresses.getBody());
//    }
//
//    @Test
//    public void getAddressInById() {
//        Mockito.doReturn(new AddressRepresentation(new BizAddress())).when(addressApplicationService).getAddressInById(anyString(), anyLong(), anyLong());
//        ResponseEntity<AddressRepresentation> addressInById = addressController.getAddressInById(rJwt(), rLong(), rLong());
//        Assert.assertNotNull(addressInById.getBody());
//    }
//
//    @Test
//    public void createAddress() {
//        BizAddress address1 = new BizAddress();
//        address1.setId(100L);
//        Mockito.doReturn(new AddressRepresentation(address1)).when(addressApplicationService).createAddress(anyString(), anyLong(), any(CreateBizAddressCommand.class));
//        ResponseEntity<Void> address = addressController.createAddress(rJwt(), rLong(), new CreateBizAddressCommand());
//        Assert.assertEquals("100", address.getHeaders().getLocation().toString());
//        Mockito.verify(addressApplicationService, Mockito.times(1)).createAddress(anyString(), anyLong(), any(CreateBizAddressCommand.class));
//    }
//
//    @Test
//    public void updateAddress() {
//        ResponseEntity<Void> address = addressController.updateAddress(rJwt(), rLong(), rLong(), new UpdateBizAddressCommand());
//        Mockito.verify(addressApplicationService, Mockito.times(1)).updateAddress(anyString(), anyLong(), anyLong(), any(UpdateBizAddressCommand.class));
//    }
//
//    @Test
//    public void deleteAddress() {
//        ResponseEntity<Void> address = addressController.deleteAddress(rJwt(), rLong(), rLong());
//        Mockito.verify(addressApplicationService, Mockito.times(1)).deleteAddress(anyString(), anyLong(), anyLong());
//    }

}