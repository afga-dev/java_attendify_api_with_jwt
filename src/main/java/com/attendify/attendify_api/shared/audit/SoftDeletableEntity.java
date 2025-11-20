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
    private LocalDateTime deleteAt;

    @Column(name = "deleted_by")
    private Long deleteBy;

    public void softDelete(Long userId) {
        this.deleteAt = LocalDateTime.now();
        this.deleteBy = userId;
    }

    public boolean isDeleted() {
        return deleteAt != null;
    }
}
