package com.hw.aggregate.address.model;

public class BizAddressTest {

//    @Test(expected = MaxAddressCountException.class)
//    public void create_more_then_5() {
//        BizAddressRepository addressRepository = Mockito.mock(BizAddressRepository.class);
//        BizAddress address1 = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        ArrayList<BizAddress> spiedAddress = Mockito.spy(addresses);
//        Mockito.doReturn(5).when(spiedAddress).size();
//        Mockito.doReturn(address1).when(addressRepository).save(any(BizAddress.class));
//        Mockito.doReturn(spiedAddress).when(addressRepository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        BizAddress.create(rLong(), rLong(), command, addressRepository);
//    }
//
//    @Test
//    public void create() {
//        Long aLong = rLong();
//        BizAddress address1 = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        ArrayList<BizAddress> spy = Mockito.spy(addresses);
//        Mockito.doReturn(1).when(spy).size();
//        BizAddressRepository mock = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(address1).when(mock).save(any(BizAddress.class));
//        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        address1.setCity(rStr());
//        address1.setCountry(rStr());
//        address1.setFullName(rStr());
//        address1.setLine1(rStr());
//        address1.setLine2(rStr());
//        address1.setPhoneNumber(rStr());
//        address1.setPostalCode(rStr());
//        address1.setProvince(rStr());
//        address1.setId(aLong);
//        BizAddress address = BizAddress.create(aLong, rLong(), command, mock);
//        Assert.assertEquals(address1.getCity(), address.getCity());
//        Assert.assertEquals(aLong, address.getId());
//    }
//
//    @Test(expected = DuplicateAddressException.class)
//    public void create_duplicate() {
//        Long aLong = rLong();
//        BizAddress address1 = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(address1);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(address1).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, address1);
//        BizAddress.create(aLong, rLong(), command, repository);
//    }
//
//    @Test
//    public void create_duplicate_dif_country() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setCountry(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_city() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setCity(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_province() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setProvince(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_phone() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setPhoneNumber(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_postCode() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setPostalCode(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_fullname() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setFullName(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_lin1() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setLine2(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setLine1(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test
//    public void create_duplicate_dif_lin2() {
//        Long aLong = rLong();
//        BizAddress stored = new BizAddress();
//        ArrayList<BizAddress> addresses = new ArrayList<>();
//        addresses.add(stored);
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Mockito.doReturn(stored).when(repository).save(any(BizAddress.class));
//        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
//        UserCreateBizAddressCommand command = new UserCreateBizAddressCommand();
//        command.setCity(rStr());
//        command.setCountry(rStr());
//        command.setFullName(rStr());
//        command.setLine1(rStr());
//        command.setPhoneNumber(rStr());
//        command.setPostalCode(rStr());
//        command.setProvince(rStr());
//        BeanUtils.copyProperties(command, stored);
//        stored.setLine2(rStr());
//        BizAddress address = BizAddress.create(aLong, rLong(), command, repository);
//        Assert.assertNotNull(address);
//    }
//
//    @Test(expected = AddressNotExistException.class)
//    public void get() {
//        BizAddressRepository mock = Mockito.mock(BizAddressRepository.class);
//        Long aLong = rLong();
//        Long pLong = rLong();
//        Mockito.doReturn(Optional.empty()).when(mock).findById(anyLong());
//        BizAddress.get(pLong, aLong, mock);
//    }
//
//    @Test
//    public void get_w_right_access() {
//        BizAddressRepository mock = Mockito.mock(BizAddressRepository.class);
//        Long aLong = rLong();
//        Long pLong = rLong();
//        BizAddress address = new BizAddress();
//        address.setProfileId(pLong);
//        Mockito.doReturn(Optional.of(address)).when(mock).findById(anyLong());
//        BizAddress address1 = BizAddress.get(pLong, aLong, mock);
//        Assert.assertNotNull(address1);
//    }
//
//    @Test(expected = AddressAccessException.class)
//    public void get_wrong_access() {
//        BizAddressRepository mock = Mockito.mock(BizAddressRepository.class);
//        Long aLong = rLong();
//        Long pLong = rLong();
//        BizAddress address = new BizAddress();
//        address.setProfileId(pLong);
//        Mockito.doReturn(Optional.of(address)).when(mock).findById(anyLong());
//        BizAddress.get(rLong(), aLong, mock);
//    }
//
//    @Test
//    public void update() {
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Long aLong = rLong();
//        Long pLong = rLong();
//        UserUpdateBizAddressCommand updateAddressCommand = new UserUpdateBizAddressCommand();
//        updateAddressCommand.setCity(rStr());
//        BizAddress address = new BizAddress();
//        address.setProfileId(pLong);
//        Mockito.doReturn(Optional.of(address)).when(repository).findById(anyLong());
//        Mockito.doReturn(address).when(repository).save(any(BizAddress.class));
//        BizAddress update = BizAddress.replace(pLong, aLong, repository, updateAddressCommand);
//        Assert.assertEquals(updateAddressCommand.getCity(), update.getCity());
//    }
//
//    @Test
//    public void delete() {
//        BizAddressRepository repository = Mockito.mock(BizAddressRepository.class);
//        Long aLong = rLong();
//        Long pLong = rLong();
//        UserUpdateBizAddressCommand updateAddressCommand = new UserUpdateBizAddressCommand();
//        updateAddressCommand.setCity(rStr());
//        BizAddress address = new BizAddress();
//        address.setProfileId(pLong);
//        Mockito.doReturn(Optional.of(address)).when(repository).findById(anyLong());
//        Mockito.doNothing().when(repository).deleteById(anyLong());
//        BizAddress.delete(pLong, aLong, repository);
//        Mockito.verify(repository, Mockito.times(1)).deleteById(anyLong());
//    }

}