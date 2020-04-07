package com.hw.aggregate.profile;

import com.hw.aggregate.profile.command.CreateProfileCommand;
import com.hw.aggregate.profile.representation.ProfileRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = "application/json")
public class ProfileController {

    @Autowired
    private ProfileApplicationService profileApplicationService;

    @GetMapping("profiles/search")
    public ResponseEntity<?> searchProfile(@RequestHeader("authorization") String authorization) {
        return ResponseEntity.ok(profileApplicationService.searchProfile(authorization));
    }

    @PostMapping("profiles")
    public ResponseEntity<?> createProfile(@RequestHeader("authorization") String authorization) {
        ProfileRepresentation profile1 = profileApplicationService.createProfile(new CreateProfileCommand(authorization));
        return ResponseEntity.ok().header("Location", profile1.profileId).build();
    }
}
