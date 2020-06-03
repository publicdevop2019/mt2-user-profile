package com.hw.aggregate.profile.representation;

import com.hw.aggregate.profile.model.Profile;
import org.junit.Assert;
import org.junit.Test;

import static com.hw.aggregate.Helper.rLong;

public class ProfileRepresentationTest {

    @Test
    public void getProfileId() {
        Profile profile = new Profile();
        profile.setId(rLong());
        ProfileRepresentation profileRepresentation = new ProfileRepresentation(profile);
        Assert.assertEquals(profile.getId().toString(), profileRepresentation.getProfileId());
    }
}