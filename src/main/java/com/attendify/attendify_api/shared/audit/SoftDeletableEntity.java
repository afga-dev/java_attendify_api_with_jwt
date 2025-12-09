package com.attendify.attendify_api.shared.audit;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class SoftDeletableEntity extends AuditableEntity {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    public void softDelete(Long userId) {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.deletedBy = userId;
        }
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
