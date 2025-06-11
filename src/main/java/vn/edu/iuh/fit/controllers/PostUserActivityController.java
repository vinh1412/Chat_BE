/**
 * @ (#) PostUserActivityController.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.PostUserActivity;
import vn.edu.iuh.fit.services.PostUserActivityService;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/postactivity")
public class PostUserActivityController {

    @Autowired
    private PostUserActivityService postUserActivityService;

    //save
     @PostMapping("/save")
    public ResponseEntity<?> savePostUserActivity(@RequestBody PostUserActivity postUserActivity) {
         if (postUserActivity == null) {
             return ResponseEntity.badRequest().body("PostUserActivity cannot be null");
         }

         try {
             PostUserActivity savedActivity = postUserActivityService.savePostUserActivity(postUserActivity);
             return ResponseEntity.ok(savedActivity);
         } catch (Exception e) {
             return ResponseEntity.status(500).body("Error saving post user activity: " + e.getMessage());
         }
     }

    // lấy tất cả hoạt động của người dùng theo postId
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getActivitiesByPostId(@PathVariable ObjectId postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("Post ID cannot be null");
        }

        try {
            return ResponseEntity.ok(postUserActivityService.findByPostId(postId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving post user activities: " + e.getMessage());
        }
     }

    // lấy danh sách hoạt động của người dùng theo postId
    @GetMapping("/post/comment/{postId}")
    public ResponseEntity<?> getActivitiesByPostIdAndActivityType(@PathVariable ObjectId postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("Post ID cannot be null");
        }

        try {
            return ResponseEntity.ok(postUserActivityService.findByPostIdAndActivityType(postId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving post user activities: " + e.getMessage());
        }
    }

    // lấy tổng số lượt comment theo postId
    @GetMapping("/post/comment/count/{postId}")
    public ResponseEntity<?> countCommentsByPostId(@PathVariable ObjectId postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("Post ID cannot be null");
        }

        try {
            long count = postUserActivityService.countCommentsByPostId(postId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error counting comments: " + e.getMessage());
        }
    }

    // cập nhật trạng thái like
    @PutMapping("/post/{postId}/user/{userId}/tym")
    public ResponseEntity<?> updateLikeStatus(@PathVariable ObjectId postId, @PathVariable ObjectId userId) {

        if ( userId == null) {
            return ResponseEntity.badRequest().body("Post ID and User ID cannot be null");
        }

        try {
            PostUserActivity updatedActivity = postUserActivityService.updateLikeStatus(postId, userId);
            return ResponseEntity.ok(updatedActivity);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating like status: " + e.getMessage());
        }
    }

    // lấy trạng thái bài viết theo userId và postId
    @GetMapping("/post/{postId}/user/{userId}/tym")
    public ResponseEntity<?> getPostLikedStatus(@PathVariable ObjectId postId, @PathVariable ObjectId userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Post ID and User ID cannot be null");
        }

        try {
            PostUserActivity activity = postUserActivityService.findByPostIdAndUserId(postId, userId);
            boolean liked = activity != null && activity.isTym(); // Giả sử isLiked() trả về trạng thái like
            return ResponseEntity.ok(Collections.singletonMap("liked", liked));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving post liked status: " + e.getMessage());
        }
    }

    // xóa bình luận theo id activity
    @DeleteMapping("/activity/{activityId}")
    public ResponseEntity<?> deleteActivityById(@PathVariable ObjectId activityId) {
        if (activityId == null) {
            return ResponseEntity.badRequest().body("Activity ID cannot be null");
        }

        try {
            postUserActivityService.deleteById(activityId);
            return ResponseEntity.ok("Activity deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting activity: " + e.getMessage());
        }
    }

    // cập nhật bình luận theo id activity
    @PutMapping("/activity/{activityId}/comment/{comment}")
    public ResponseEntity<?> updateCommentById(@PathVariable ObjectId activityId, @PathVariable String comment) {
        if (activityId == null || comment == null || comment.isEmpty()) {
            return ResponseEntity.badRequest().body("Activity ID and comment cannot be null or empty");
        }

        try {
            PostUserActivity updatedActivity = postUserActivityService.updateCommentById(activityId, comment);
            return ResponseEntity.ok(updatedActivity);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating comment: " + e.getMessage());
        }
    }

}