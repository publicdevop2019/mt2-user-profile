package com.hw.repo;


import com.hw.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepo extends JpaRepository<Profile, Long> {
    @Query("SELECT p FROM Profile p WHERE p.resourceOwnerId = ?1")
    Optional<Profile> findProfileByResourceOwnerId(Long resourceOwnerId);
}
