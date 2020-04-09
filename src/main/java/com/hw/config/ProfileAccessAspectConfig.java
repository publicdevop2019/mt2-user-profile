package com.hw.config;

import com.hw.aggregate.profile.ProfileRepo;
import com.hw.aggregate.profile.model.Profile;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Aspect
@Slf4j
public class ProfileAccessAspectConfig {
    @Autowired
    ProfileRepo profileRepo;

    @Pointcut("@annotation(com.hw.clazz.ProfileExistAndOwnerOnly)")
    public void restrictAccess() {
    }

    @Around(value = "com.hw.config.ProfileAccessAspectConfig.restrictAccess()")
    public Object aroundGetAllPayments(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String resourceOwnerId = (String) args[0];
        Long profileId = (Long) args[1];
        Optional<Profile> byId = profileRepo.findById(profileId);
        if (byId.isEmpty()) {
            log.info("resource not found");
            throw new IllegalArgumentException("resource not found");
        } else if (byId.get().getCreatedBy().equals(resourceOwnerId)) {
            return joinPoint.proceed();
        } else {
            log.info("you can only view/modify your own data");
            throw new IllegalArgumentException("you can only view/modify your own data");
        }

    }
}