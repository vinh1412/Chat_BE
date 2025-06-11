/**
 * @ (#) PostReponse.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostReponse {
    private String id;
    private String content;
    private String userId;
    private String createdAt;
    private String updatedAt;
    private int likeCount;
    private int commentCount;
}