package com.hw.aggregate.profile.representation;

import com.hw.aggregate.profile.model.Profile;

public class ProfileRepresentation {
    public String profileId;

    public ProfileRepresentation(Profile profile) {
        profileId = profile.getId().toString();
    }
}
