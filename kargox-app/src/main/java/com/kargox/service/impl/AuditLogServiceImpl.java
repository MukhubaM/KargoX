package com.kargox.service.impl;

import com.kargox.entity.AuditLog;
import com.kargox.repository.AuditLogRepository;
import com.kargox.security.SecurityUser;
import com.kargox.service.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String entityType, Long entityId, String details) {

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof SecurityUser su) {
            log.setActorUserId(su.getId());
            log.setActorEmail(su.getEmail());
            log.setActorRole(su.getRole());
        } else {
            log.setActorEmail("SYSTEM");
            log.setActorRole("SYSTEM");
        }

        auditLogRepository.save(log);
    }
}
