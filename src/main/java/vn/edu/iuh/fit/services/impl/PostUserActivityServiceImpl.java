/**
 * @ (#) PostUserActivityServiceImpl.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.PostUserActivity;
import vn.edu.iuh.fit.repositories.PostUserActivityRepository;
import vn.edu.iuh.fit.services.PostUserActivityService;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
@Service
public class PostUserActivityServiceImpl implements PostUserActivityService {

    @Autowired
    private PostUserActivityRepository postUserActivityRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PostUserActivity savePostUserActivity(PostUserActivity postUserActivity) {
        // Thiết lập thời gian hành động nếu chưa có
        if (postUserActivity.getActivityTime() == null) {
            postUserActivity.setActivityTime(java.time.LocalDateTime.now());
        }
        // comment khng null
        if (postUserActivity.getComment() == null) {
            postUserActivity.setComment("ok");
        }


        // Lưu vào MongoDB qua repository
        return postUserActivityRepository.save(postUserActivity);
    }

    @Override
    public List<PostUserActivity> findByPostId(ObjectId postId) {
        // Sử dụng MongoTemplate để tìm tất cả hoạt động của người dùng theo postId
        return mongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                ),
                PostUserActivity.class
        );
    }

    @Override
    public List<PostUserActivity> findByPostIdAndActivityType(ObjectId postId) {
        // Sử dụng MongoTemplate để tìm tất cả hoạt động của người dùng theo postId và activityType
        return mongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                ),
                PostUserActivity.class
        );

    }

    @Override
    public long countCommentsByPostId(ObjectId postId) {
        // Sử dụng MongoTemplate để đếm số lượng bình luận theo postId
        return mongoTemplate.count(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                                .and("comment").exists(true)
                ),
                PostUserActivity.class
        );
    }

    @Override
    public PostUserActivity updateLikeStatus(ObjectId postId, ObjectId userId) {
        // if nếu tym đã tồn tại thì bỏ tym, nếu chưa có thì update
        PostUserActivity activity = mongoTemplate.findOne(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                                .and("userIdActor").is(userId)
                ),
                PostUserActivity.class
        );
        if (activity != null) {
            // Nếu đã có hoạt động, cập nhật trạng thái tym
            activity.setTym(!activity.isTym());
            return postUserActivityRepository.save(activity);
        } else {
            // Nếu chưa có hoạt động, tạo mới
            PostUserActivity newActivity = PostUserActivity.builder()
                    .postId(postId)
                    .userIdActor(userId)
                    .tym(true) // Mặc định là thích
                    .activityTime(java.time.LocalDateTime.now())
                    .build();
            return postUserActivityRepository.save(newActivity);
        }
    }

    @Override
    public PostUserActivity findByPostIdAndUserId(ObjectId postId, ObjectId userId) {
        // Sử dụng MongoTemplate để tìm hoạt động theo postId và userId
        return mongoTemplate.findOne(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                                .and("userIdActor").is(userId)
                ),
                PostUserActivity.class
        );
    }

    @Override
    public void deleteById(ObjectId activityId) {
        // Xóa hoạt động theo ID
        postUserActivityRepository.deleteById(activityId);

    }

    @Override
    public PostUserActivity updateCommentById(ObjectId activityId, String comment) {
        // Tìm hoạt động theo ID
        PostUserActivity activity = postUserActivityRepository.findById(activityId).orElse(null);
        if (activity != null) {
            // Cập nhật bình luận
            activity.setComment(comment);
            return postUserActivityRepository.save(activity);
        }
        return null; // Trả về null nếu không tìm thấy hoạt động
    }


}