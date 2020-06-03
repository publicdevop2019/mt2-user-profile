package com.hw.aggregate.profile;

import com.hw.aggregate.profile.model.Profile;
import com.hw.aggregate.profile.representation.ProfileRepresentation;
import com.hw.shared.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileApplicationService {

    @Autowired
    private ProfileRepo profileRepo;
    @Autowired
    private IdGenerator idGenerator;

    @Transactional(readOnly = true)
    public ProfileRepresentation searchProfile(String resourceOwnerId) {
        return new ProfileRepresentation(Profile.get(resourceOwnerId, profileRepo));
    }

    @Transactional
    public ProfileRepresentation createProfile(String resourceOwnerId) {
        return new ProfileRepresentation(Profile.create(resourceOwnerId, profileRepo, idGenerator.getId()));
    }
}
