/**
 * @ (#) PostUserDTO.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos;
import lombok.*;
import vn.edu.iuh.fit.entities.Post;
import vn.edu.iuh.fit.entities.User;
import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostUserDTO {
    private User user;
    private List<Post> posts;
}