package com.hw.aggregate.profile;

import com.hw.aggregate.profile.representation.ProfileRepresentation;
import com.hw.shared.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = "application/json")
public class ProfileController {

    @Autowired
    private ProfileApplicationService profileApplicationService;

    @GetMapping("profiles/search")
    public ResponseEntity<String> searchProfile(@RequestHeader("authorization") String authorization) {
        return ResponseEntity.ok(profileApplicationService.searchProfile(ServiceUtility.getUserId(authorization)).getProfileId());
    }

    @PostMapping("profiles")
    public ResponseEntity<Void> createProfile(@RequestHeader("authorization") String authorization) {
        ProfileRepresentation profile1 = profileApplicationService.createProfile(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok().header("Location", profile1.getProfileId()).build();
    }
}
