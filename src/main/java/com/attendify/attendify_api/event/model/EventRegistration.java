package com.attendify.attendify_api.event.model;

import org.hibernate.annotations.SQLRestriction;

import com.attendify.attendify_api.shared.audit.SoftDeletableEntity;
import com.attendify.attendify_api.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_registrations", indexes = {
                @Index(name = "idx_event_registration_event", columnList = "event_id"),
                @Index(name = "idx_event_registration_user", columnList = "user_id"),
}, uniqueConstraints = {
                @UniqueConstraint(columnNames = { "user_id", "event_id" })
})
@SQLRestriction("deleted_at IS NULL")
public class EventRegistration extends SoftDeletableEntity {
        @EqualsAndHashCode.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "event_registration_id")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "event_id", nullable = false)
        private Event event;

        @Builder.Default
        @NotNull
        @Column(nullable = false)
        private Boolean checkedIn = false;
}
