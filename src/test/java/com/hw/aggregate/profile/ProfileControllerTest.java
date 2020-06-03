package com.hw.aggregate.profile;

import com.hw.aggregate.profile.representation.ProfileRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import static com.hw.aggregate.Helper.rJwt;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ProfileControllerTest {
    @InjectMocks
    ProfileController profileController;
    @Mock
    ProfileApplicationService profileApplicationService;

    @Test
    public void searchProfile() {
        ProfileRepresentation mock = Mockito.mock(ProfileRepresentation.class);
        Mockito.doReturn(mock).when(profileApplicationService).searchProfile(anyString());
        Mockito.doReturn("mock").when(mock).getProfileId();
        ResponseEntity<String> stringResponseEntity = profileController.searchProfile(rJwt());
        Assert.assertNotNull(stringResponseEntity.getBody());
    }

    @Test
    public void createProfile() {
        ProfileRepresentation mock = Mockito.mock(ProfileRepresentation.class);
        Mockito.doReturn(mock).when(profileApplicationService).createProfile(anyString());
        Mockito.doReturn("mock").when(mock).getProfileId();
        ResponseEntity<Void> profile = profileController.createProfile(rJwt());
        Assert.assertNotNull(profile.getHeaders().getLocation());
    }
}