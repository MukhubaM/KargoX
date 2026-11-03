package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    private Long actorUserId;
    private String actorEmail;
    private String actorRole;

    @Column(nullable = false)
    private String action;

    private String entityType;
    private Long entityId;

    @Column(length = 2000)
    private String details;

    private String ipAddress;
}
