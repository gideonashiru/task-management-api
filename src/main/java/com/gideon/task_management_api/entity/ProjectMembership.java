package com.gideon.task_management_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "project_id", "member_id" })
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false)
    private ProjectRole role;

    @PrePersist
    protected void onJoin() {
        joinedAt = LocalDateTime.now();
        if (role == null) {
            role = ProjectRole.MEMBER;
        }
    }
}
