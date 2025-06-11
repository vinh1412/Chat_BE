/**
 * @ (#) PostRepository.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Post;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
@Repository
public interface PostRepository extends MongoRepository<Post, ObjectId> {

    // Tìm tất cả bài viết của một người dùng
    List<Post> findByUserId(ObjectId userId);

    // lấy câu query lấy ra thông tin user và bài viết của họ


}