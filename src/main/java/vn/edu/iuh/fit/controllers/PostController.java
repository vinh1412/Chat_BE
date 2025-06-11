/**
 * @ (#) PostController.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.PostUserDTO;
import vn.edu.iuh.fit.dtos.request.PostRequest;
import vn.edu.iuh.fit.entities.Post;
import vn.edu.iuh.fit.services.PostService;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> savePost(@RequestPart("request") String postRequestJson,
                                      @RequestPart(value = "image", required = false) MultipartFile image) {

        ObjectMapper objectMapper = new ObjectMapper();
        PostRequest postRequest;

//        if (post == null) {
//            return ResponseEntity.badRequest().body("Post cannot be null");
//        }

        try {
            postRequest = objectMapper.readValue(postRequestJson, PostRequest.class);
            if (postRequest == null) {
                return ResponseEntity.badRequest().body("Post cannot be null");
            }
            System.out.println("PostRequest: " + postRequest);

            Post savedPost = postService.savePost(postRequest, image);  // gửi nguyên object
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving post: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable String userId) {
        try {
            ObjectId objectId = new ObjectId(userId);
            return ResponseEntity.ok(postService.findByUserId(objectId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid userId format");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving posts: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPosts() {
        try {
            return ResponseEntity.ok(postService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving posts: " + e.getMessage());
        }
    }

    @GetMapping("/users-with-posts")
    public ResponseEntity<?> getUsersWithPosts() {
        try {
            List<PostUserDTO> postUserDTOS = postService.findUsersWithPosts();
            System.out.println("PostUserDTOs: " + postUserDTOS);
            return ResponseEntity.ok(postUserDTOS);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users with posts: " + e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePostById(@PathVariable String postId) {
        try {
            ObjectId objectId = new ObjectId(postId);
            postService.deletePostById(objectId);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid postId format");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting post: " + e.getMessage());
        }
    }

    @PutMapping("/update/{idPost}")
    public ResponseEntity<?> updatePost(@PathVariable String idPost, @RequestBody Post post) {
        try {
            ObjectId objectId = new ObjectId(idPost);
            post.setId(objectId); // Set the ID for the post to update
            Post updatedPost = postService.updatePost(post);
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid postId format");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating post: " + e.getMessage());
        }
    }




}