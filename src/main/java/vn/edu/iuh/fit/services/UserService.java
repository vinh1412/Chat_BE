/*
 * @ {#} UserService.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
public interface UserService {
    UserResponse getUserByPhone(String phone);
    boolean existsByPhone(String phone);
    void save(User user);
    boolean isPasswordValid(String phone, String password);
    void updatePassword(String phone, String newPassword);

    UserResponse getCurrentUser(String token);

    UserResponse updateUser(ObjectId userId, UpdateUserRequest request);
    UserResponse changePassword(ObjectId userId, String oldPassword, String newPassword);

    public List<UserResponse> searchByKeyWord(String keyWord);
    List<UserResponse> getUsersByIds(Set<ObjectId> userIds);

    UserResponse getUserById(ObjectId userId);

    Optional<User> getUserByIds(ObjectId userId);
}
