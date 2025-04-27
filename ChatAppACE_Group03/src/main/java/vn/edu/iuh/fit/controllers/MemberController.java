/*
 * @ {#} MemberController.java   1.0     23/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   23/04/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.services.MemberService;
import vn.edu.iuh.fit.services.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final UserService userService;

    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(@RequestHeader("Authorization") String token, @RequestParam ObjectId conversationId, @RequestParam(required = false) ObjectId userId) {

        try {
            UserResponse currentUser = userService.getCurrentUser(token);

            ObjectId targetUserId = userId != null ? userId : currentUser.getId();

            MemberRoles role = memberService.getUserRoleInConversation(targetUserId, conversationId);

            return ResponseEntity.ok(Map.of("role", role.name()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
