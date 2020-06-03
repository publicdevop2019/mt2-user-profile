package com.hw.aggregate.address.model;

import com.hw.aggregate.address.AddressRepository;
import com.hw.aggregate.address.command.CreateAddressCommand;
import com.hw.aggregate.address.command.UpdateAddressCommand;
import com.hw.aggregate.address.exception.AddressAccessException;
import com.hw.aggregate.address.exception.AddressNotExistException;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Optional;

import static com.hw.aggregate.Helper.rLong;
import static com.hw.aggregate.Helper.rStr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class AddressTest {

    @Test(expected = MaxAddressCountException.class)
    public void create_more_then_5() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        Address address1 = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        ArrayList<Address> spiedAddress = Mockito.spy(addresses);
        Mockito.doReturn(5).when(spiedAddress).size();
        Mockito.doReturn(address1).when(addressRepository).save(any(Address.class));
        Mockito.doReturn(spiedAddress).when(addressRepository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        Address.create(rLong(), rLong(), command, addressRepository);
    }

    @Test
    public void create() {
        Long aLong = rLong();
        Address address1 = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        ArrayList<Address> spy = Mockito.spy(addresses);
        Mockito.doReturn(1).when(spy).size();
        AddressRepository mock = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(address1).when(mock).save(any(Address.class));
        Mockito.doReturn(spy).when(mock).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        address1.setCity(rStr());
        address1.setCountry(rStr());
        address1.setFullName(rStr());
        address1.setLine1(rStr());
        address1.setLine2(rStr());
        address1.setPhoneNumber(rStr());
        address1.setPostalCode(rStr());
        address1.setProvince(rStr());
        address1.setId(aLong);
        Address address = Address.create(aLong, rLong(), command, mock);
        Assert.assertEquals(address1.getCity(), address.getCity());
        Assert.assertEquals(aLong, address.getId());
    }

    @Test(expected = DuplicateAddressException.class)
    public void create_duplicate() {
        Long aLong = rLong();
        Address address1 = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(address1).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, address1);
        Address.create(aLong, rLong(), command, repository);
    }

    @Test
    public void create_duplicate_dif_country() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setCountry(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_city() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setCity(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_province() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setProvince(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_phone() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setPhoneNumber(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_postCode() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setPostalCode(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_fullname() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setFullName(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_lin1() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setLine2(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setLine1(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test
    public void create_duplicate_dif_lin2() {
        Long aLong = rLong();
        Address stored = new Address();
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(stored);
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Mockito.doReturn(stored).when(repository).save(any(Address.class));
        Mockito.doReturn(addresses).when(repository).findByProfileId(anyLong());
        CreateAddressCommand command = new CreateAddressCommand();
        command.setCity(rStr());
        command.setCountry(rStr());
        command.setFullName(rStr());
        command.setLine1(rStr());
        command.setPhoneNumber(rStr());
        command.setPostalCode(rStr());
        command.setProvince(rStr());
        BeanUtils.copyProperties(command, stored);
        stored.setLine2(rStr());
        Address address = Address.create(aLong, rLong(), command, repository);
        Assert.assertNotNull(address);
    }

    @Test(expected = AddressNotExistException.class)
    public void get() {
        AddressRepository mock = Mockito.mock(AddressRepository.class);
        Long aLong = rLong();
        Long pLong = rLong();
        Mockito.doReturn(Optional.empty()).when(mock).findById(anyLong());
        Address.get(pLong, aLong, mock);
    }

    @Test
    public void get_w_right_access() {
        AddressRepository mock = Mockito.mock(AddressRepository.class);
        Long aLong = rLong();
        Long pLong = rLong();
        Address address = new Address();
        address.setProfileId(pLong);
        Mockito.doReturn(Optional.of(address)).when(mock).findById(anyLong());
        Address address1 = Address.get(pLong, aLong, mock);
        Assert.assertNotNull(address1);
    }

    @Test(expected = AddressAccessException.class)
    public void get_wrong_access() {
        AddressRepository mock = Mockito.mock(AddressRepository.class);
        Long aLong = rLong();
        Long pLong = rLong();
        Address address = new Address();
        address.setProfileId(pLong);
        Mockito.doReturn(Optional.of(address)).when(mock).findById(anyLong());
        Address.get(rLong(), aLong, mock);
    }

    @Test
    public void update() {
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Long aLong = rLong();
        Long pLong = rLong();
        UpdateAddressCommand updateAddressCommand = new UpdateAddressCommand();
        updateAddressCommand.setCity(rStr());
        Address address = new Address();
        address.setProfileId(pLong);
        Mockito.doReturn(Optional.of(address)).when(repository).findById(anyLong());
        Mockito.doReturn(address).when(repository).save(any(Address.class));
        Address update = Address.update(pLong, aLong, repository, updateAddressCommand);
        Assert.assertEquals(updateAddressCommand.getCity(), update.getCity());
    }

    @Test
    public void delete() {
        AddressRepository repository = Mockito.mock(AddressRepository.class);
        Long aLong = rLong();
        Long pLong = rLong();
        UpdateAddressCommand updateAddressCommand = new UpdateAddressCommand();
        updateAddressCommand.setCity(rStr());
        Address address = new Address();
        address.setProfileId(pLong);
        Mockito.doReturn(Optional.of(address)).when(repository).findById(anyLong());
        Mockito.doNothing().when(repository).deleteById(anyLong());
        Address.delete(pLong, aLong, repository);
        Mockito.verify(repository, Mockito.times(1)).deleteById(anyLong());
    }

}