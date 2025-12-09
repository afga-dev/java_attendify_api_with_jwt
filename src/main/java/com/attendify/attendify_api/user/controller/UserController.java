package com.attendify.attendify_api.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDTO> getMyUser() {
        return ResponseEntity.ok(userService.getMyUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_FORCE_READ')")
    public ResponseEntity<UserSummaryDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        userService.delete();

        SecurityContextHolder.clearContext();

        // No content returned after successful deletion
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_FORCE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);

        // No content returned after successful deletion
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_RESTORE')")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        userService.restore(id);

        // No content returned after successful restoration
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('USER_READ_ALL')")
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('USER_READ_DELETED')")
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAllDeleted(Pageable pageable) {
        return ResponseEntity.ok(userService.findAllDeleted(pageable));
    }

    @GetMapping("/with-deleted")
    @PreAuthorize("hasAuthority('USER_READ_WITH_DELETED')")
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAllWithDeleted(Pageable pageable) {
        return ResponseEntity.ok(userService.findAllWithDeleted(pageable));
    }
}
