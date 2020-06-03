package com.hw.aggregate.profile.model;

import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.exception.ProfileAlreadyExistException;
import com.hw.aggregate.profile.exception.ProfileNotExistException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static com.hw.aggregate.Helper.rLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class ProfileTest {

    @Test(expected = ProfileNotExistException.class)
    public void get() {
        ProfileRepo mock = Mockito.mock(ProfileRepo.class);
        Mockito.doReturn(Optional.empty()).when(mock).findProfileByResourceOwnerId(anyLong());
        Profile profile = Profile.get(rLong().toString(), mock);
    }

    @Test
    public void get_success() {
        ProfileRepo mock = Mockito.mock(ProfileRepo.class);
        Profile mock1 = Mockito.mock(Profile.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findProfileByResourceOwnerId(anyLong());
        Profile profile = Profile.get(rLong().toString(), mock);
        Assert.assertNotNull(profile);
    }

    @Test(expected = ProfileAlreadyExistException.class)
    public void create() {
        ProfileRepo mock = Mockito.mock(ProfileRepo.class);
        Profile mock1 = Mockito.mock(Profile.class);
        Mockito.doReturn(Optional.of(mock1)).when(mock).findProfileByResourceOwnerId(anyLong());
        Profile profile = Profile.create(rLong().toString(), mock, rLong());
    }

    @Test
    public void create_success() {
        ProfileRepo mock = Mockito.mock(ProfileRepo.class);
        Profile mock1 = Mockito.mock(Profile.class);
        Mockito.doReturn(Optional.empty()).when(mock).findProfileByResourceOwnerId(anyLong());
        Mockito.doReturn(mock1).when(mock).save(any(Profile.class));
        Profile profile = Profile.create(rLong().toString(), mock, rLong());
        Assert.assertNotNull(profile);
    }
}