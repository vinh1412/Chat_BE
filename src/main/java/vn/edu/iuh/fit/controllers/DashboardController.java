/*
 * @ {#} DashboardController.java   1.0     17/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/03/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/welcome-message")
    public ResponseEntity<String> dashboard(Authentication authentication) {
        return ResponseEntity.ok("Welcome " + authentication.getName() + " to the dashboard!"+" with scope "+authentication.getAuthorities());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin-message")
    public ResponseEntity<String> admin(Authentication authentication) {
        return ResponseEntity.ok("Welcome " + authentication.getName() + " to the admin dashboard!"+" with scope "+authentication.getAuthorities());
    }
}
