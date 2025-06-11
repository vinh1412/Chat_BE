/**
 * @ (#) PostUserActivityRepository.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.repositories;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.PostUserActivity;

@Repository
public interface PostUserActivityRepository extends MongoRepository<PostUserActivity, ObjectId> {

//    xóa bình luận theo postId
    void deleteByPostId(ObjectId postId);

}