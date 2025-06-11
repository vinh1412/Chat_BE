/**
 * @ (#) UserController.java      4/9/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ChangePasswordRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.FriendResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/9/2025
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private Validator validator;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private FriendService friendService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            UserResponse response = userService.getCurrentUser(token);

            simpMessagingTemplate.convertAndSend("/user/profile/" + response.getId(), response);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get current user").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping(value = "/me/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateUser(
            @RequestPart("request") String reqJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar ,
            @RequestHeader("Authorization") String token)
    {
        System.out.println("Request update user: " + reqJson);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UpdateUserRequest requestUpdate = null;
        try {
            requestUpdate = objectMapper.readValue(reqJson, UpdateUserRequest.class);
            String avatarUrl = null;
            if(avatar != null && !avatar.isEmpty()) {
                avatarUrl = cloudinaryService.uploadImage(avatar);
                requestUpdate.setAvatar(avatarUrl);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message("Invalid request format")
                            .build());
        }

        // Validate the request
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(requestUpdate);
        if(!violations.isEmpty()) {
            Map<String, Object> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Validation failed")
                    .response(errors)
                    .build());
        }

        try {
            System.out.println("Request update: " + requestUpdate.toString() + " token: " + token);
            UserResponse user = userService.getCurrentUser(token);
            UserResponse userUpdate = userService.updateUser(user.getId(), requestUpdate);

            // Gui tin nhan den WebSocket
            System.out.println("Sending to destination: /user/profile/" + user.getId());
            System.out.println("Data: " + userUpdate);
            simpMessagingTemplate.convertAndSend("/user/profile/" + user.getId(), userUpdate);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Update user successfully").response(userUpdate).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequest request, @RequestHeader("Authorization") String token) {
        try {
            UserResponse currentUser = userService.getCurrentUser(token);
            UserResponse updated = userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("SUCCESS")
                            .message("Change password successfully")
                            .response(updated)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build()
                    );
        }
    }


    @PostMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhone(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = userService.existsByPhone(phone);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-friends")
    public ResponseEntity<ApiResponse<?>> getMyFriends(@RequestHeader("Authorization") String token) {
        try {
            UserResponse currentUser = userService.getCurrentUser(token);
            System.out.println("Current user: " + currentUser.getId());
            List<FriendResponse> friends = friendService.getFriends(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get my friends successfully").response(friends).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> search(@RequestParam("keyword") String keyword) {
        try {
            System.out.println("KeyWord: " + keyword);
            List<UserResponse> userResponses = userService.searchByKeyWord(keyword);
            if(userResponses.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Not Found User").response("").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Search user success").response(userResponses).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }


    @GetMapping("/me/{token}")
    public ResponseEntity<ApiResponse<?>> getCurrentUserToken(@PathVariable("token") String token) {
        try {
            UserResponse response = userService.getCurrentUser(token);

            simpMessagingTemplate.convertAndSend("/user/profile/" + response.getId(), response);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get current user").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }
    // ghetusrby id
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable("userId") ObjectId userId) {
        try {
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get user by ID successfully").response(user).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }
}
