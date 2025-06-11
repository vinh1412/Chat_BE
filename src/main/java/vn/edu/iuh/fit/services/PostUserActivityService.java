/**
 * @ (#) PostUserActivityService.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.PostUserActivity;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
public interface PostUserActivityService {
    // THEM
    PostUserActivity savePostUserActivity(PostUserActivity postUserActivity);

    // lấy tat ca theo post id
     List<PostUserActivity> findByPostId(ObjectId postId);

     // lấy danh sach h theo post id
     List<PostUserActivity> findByPostIdAndActivityType(ObjectId postId);

     // lay tong so luot comment theo post id
     long countCommentsByPostId(ObjectId postId);

     // update tym, khi biết idpost và id người dùng
     PostUserActivity updateLikeStatus(ObjectId postId, ObjectId userId);


     // lay trang thai bai viet theo userId, idpost
        PostUserActivity findByPostIdAndUserId(ObjectId postId, ObjectId userId);


        // xóa bình luận theo id activity
    void deleteById(ObjectId activityId);

    // update bình luận theo id activity
    PostUserActivity updateCommentById(ObjectId activityId, String comment);



}