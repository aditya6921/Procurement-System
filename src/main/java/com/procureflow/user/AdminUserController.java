package com.procureflow.user;

import com.procureflow.auth.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleUpdateRequest request
    ) {

        User updatedUser =
                userService.updateRole(
                        userId,
                        request.role()
                );

        return ResponseEntity.ok(
                UserResponse.from(updatedUser)
        );
    }
}
