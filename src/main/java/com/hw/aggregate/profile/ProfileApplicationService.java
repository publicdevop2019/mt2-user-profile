package com.hw.aggregate.profile;

import com.hw.aggregate.profile.command.CreateProfileCommand;
import com.hw.aggregate.profile.exception.ProfileAlreadyExistException;
import com.hw.aggregate.profile.exception.ProfileNotExistException;
import com.hw.aggregate.profile.model.Profile;
import com.hw.aggregate.profile.representation.ProfileRepresentation;
import com.hw.shared.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileApplicationService {

    @Autowired
    private ProfileRepo profileRepo;

    @Transactional(readOnly = true)
    public ProfileRepresentation searchProfile(String authorization) {
        String resourceOwnerId = ServiceUtility.getUserId(authorization);
        Optional<Profile> profileByResourceOwnerId = profileRepo.findProfileByResourceOwnerId(Long.parseLong(resourceOwnerId));
        if (profileByResourceOwnerId.isEmpty())
            throw new ProfileNotExistException();
        return new ProfileRepresentation(profileByResourceOwnerId.get());
    }

    @Transactional
    public ProfileRepresentation createProfile(CreateProfileCommand createProfileCommand) {
        String resourceOwnerId = ServiceUtility.getUserId(createProfileCommand.authorization);
        Optional<Profile> profileByResourceOwnerId = profileRepo.findProfileByResourceOwnerId(Long.parseLong(resourceOwnerId));
        if (profileByResourceOwnerId.isPresent())
            throw new ProfileAlreadyExistException();
        Profile profile = new Profile();
        profile.setResourceOwnerId(Long.parseLong(resourceOwnerId));
        Profile save = profileRepo.save(profile);
        return new ProfileRepresentation(save);
    }
}
