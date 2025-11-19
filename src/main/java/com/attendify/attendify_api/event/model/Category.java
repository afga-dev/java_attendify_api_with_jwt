package com.attendify.attendify_api.event.model;

import java.util.HashSet;
import java.util.Set;

import com.attendify.attendify_api.shared.core.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_name", columnList = "name")
})
public class Category extends AuditableEntity {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<Event> events = new HashSet<>();
}
