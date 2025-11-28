package com.attendify.attendify_api.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.shared.annotation.role.AdminOnly;
import com.attendify.attendify_api.shared.annotation.user.CanReadUser;
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
    @CanReadUser
    public ResponseEntity<UserSummaryDTO> getMyUser() {
        return ResponseEntity.ok(userService.getMyUser());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        userService.delete();

        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);

        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @AdminOnly
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        userService.restore(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    @AdminOnly
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/deleted")
    @AdminOnly
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAllDeleted(Pageable pageable) {
        return ResponseEntity.ok(userService.findAllDeleted(pageable));
    }

    @GetMapping("/all")
    @AdminOnly
    public ResponseEntity<PageResponseDTO<UserSummaryDTO>> getAllUsersIncludingDeleted(Pageable pageable) {
        return ResponseEntity.ok(userService.findAllIncludingDeleted(pageable));
    }
}
