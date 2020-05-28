package com.hw.aggregate.profile.representation;

import com.hw.aggregate.profile.model.Profile;
import lombok.Data;

@Data
public class ProfileRepresentation {
    private String profileId;

    public ProfileRepresentation(Profile profile) {
        profileId = profile.getId().toString();
    }
}
