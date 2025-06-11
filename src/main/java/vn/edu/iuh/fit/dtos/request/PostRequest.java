/**
 * @ (#) PostRequest.java      5/29/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.time.Instant;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/29/2025
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId userId;

    private String content;
    private String imageUrl; // URL của ảnh nếu có

    private Instant createdAt; // Thời gian tạo bài viết
    private Instant updatedAt; // Thời gian cập nhật bài viết

    private boolean isPublic; // Trạng thái công khai của bài viết

    private int likeCount; // Số lượt thích
    private String comment;

    private int fonts;
}