/**
 * @ (#) PostService.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.PostUserDTO;
import vn.edu.iuh.fit.dtos.request.PostRequest;
import vn.edu.iuh.fit.entities.Post;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
public interface PostService {
    public Post savePost(PostRequest post, MultipartFile file);

    // Lấy tất cả bài viết của một người dùng
    List<Post> findByUserId(ObjectId userId);

    List<Post> findAll();

    List<PostUserDTO> findUsersWithPosts();

    //xóa
    void deletePostById(ObjectId postId);

    // update
    Post updatePost(Post post);

    // xóa bài viết sẽ xóa luôn bình luận liên quan


}