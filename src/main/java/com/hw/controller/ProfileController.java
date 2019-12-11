package com.hw.controller;

import com.hw.entity.Profile;
import com.hw.repo.ProfileRepo;
import com.hw.utility.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "v1/api", produces = "application/json")
public class ProfileController {

    @Autowired
    ProfileRepo profileRepo;

    @GetMapping("profiles/search")
    public ResponseEntity<?> searchProfileByResourceOwnerId(@RequestHeader("authorization") String authorization) {
        String resourceOwnerId = ServiceUtility.getUsername(authorization);
        Optional<Profile> profileByResourceOwnerId = profileRepo.findProfileByResourceOwnerId(Long.parseLong(resourceOwnerId));
        if (profileByResourceOwnerId.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileByResourceOwnerId.get().getId());
    }

    @PostMapping("profiles")
    public ResponseEntity<?> createProfile(@RequestHeader("authorization") String authorization) {
        String resourceOwnerId = ServiceUtility.getUsername(authorization);
        Optional<Profile> profileByResourceOwnerId = profileRepo.findProfileByResourceOwnerId(Long.parseLong(resourceOwnerId));
        if (profileByResourceOwnerId.isPresent()) {
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("error", "you already created profile");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
        }
        Profile profile = new Profile();
        profile.setResourceOwnerId(Long.parseLong(resourceOwnerId));
        Profile save = profileRepo.save(profile);
        return ResponseEntity.ok().header("Location", String.valueOf(save.getId())).build();
    }
}
