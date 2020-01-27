package com.hw.config;

import com.hw.entity.Profile;
import com.hw.repo.ProfileRepo;
import com.hw.shared.ServiceUtility;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Aspect
public class AccessAspectConfig {
    @Autowired
    ProfileRepo profileRepo;

    @Pointcut("@annotation(com.hw.clazz.ProfileExistAndOwnerOnly)")
    public void restrictAccess() {
    }

    @Around(value = "com.hw.config.AccessAspectConfig.restrictAccess()")
    public Object aroundGetAllPayments(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String resourceOwnerId = ServiceUtility.getUsername((String) args[0]);
        Long profileId = (Long) args[1];
        Optional<Profile> byId = profileRepo.findById(profileId);
        if (byId.isEmpty()) {
            throw new IllegalArgumentException("resource not found");
        } else if (byId.get().getCreatedBy().equals(resourceOwnerId)) {
            return joinPoint.proceed();
        } else {
            throw new IllegalArgumentException("you can only view/modify your own data");
        }

    }
}
