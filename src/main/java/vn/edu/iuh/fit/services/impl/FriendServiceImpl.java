/**
 * @ (#) FriendServiceImpl.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.FriendResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Friend;
import vn.edu.iuh.fit.entities.FriendRequest;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.FriendRequestException;
import vn.edu.iuh.fit.exceptions.UserNotFoundException;
import vn.edu.iuh.fit.repositories.FileRepository;
import vn.edu.iuh.fit.repositories.FriendRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.util.*;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@Service
public class FriendServiceImpl implements FriendService {
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Lấy danh sách bạn bè của người dùng
     * Kiem tra friend dong thoi ca 2 chieu
     * findByUserId: Lay danh sach user tu user_id
     * findByFriendId: Lay danh sach friend tu user_id truyen vao
     * @param userId userId can lay danh sach ban be
     * @return
     */
    @Override
    public List<FriendResponse> getFriends(ObjectId userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        // Lấy danh sách bạn bè từ user_id
        List<Friend> fromUserId = friendRepository.findByUserId(userId);
        System.out.println("fromUserId: " + fromUserId);

        // Lấy danh sách bạn bè từ friend_id
        List<Friend> fromFriendId = friendRepository.findByFriendId(userId);

        System.out.println("fromFriendId: " + fromFriendId);

        // Luu danh sach id cua ban be
        Set<ObjectId> friendIds = new HashSet<>();

        // Luu danh sach friendid cua ban be tu user_id
        if(!fromUserId.isEmpty()) {
            friendIds.addAll(fromUserId.stream().map(Friend::getFriendId).filter(Objects::nonNull).toList());
        }

        // Luu danh sach userid cua ban be tu friend_id
        if (!fromFriendId.isEmpty()) {
            friendIds.addAll(fromFriendId.stream().map(Friend::getUserId).filter(Objects::nonNull).toList());
        }

        System.out.println("friendIds: " + friendIds);
        List<User> friends = userRepository.findAllById(friendIds);
        System.out.println("friends: " + friends);

        if (friends.size() != friendIds.size()) {
            throw new UserNotFoundException("One or more users not found");
        }

        return friends.stream()
                .map(friend -> {
                    FriendResponse friendResponse = new FriendResponse();
                    friendResponse.setUserId(friend.getId());
                    friendResponse.setDisplayName(friend.getDisplayName());
                    friendResponse.setAvatar(friend.getAvatar());
                    return friendResponse;
                })
                .collect(Collectors.toList());

    }

    /**
     * Kiem tra xem 2 user co phai la ban be khong
     * @param userId
     * @param friendId
     * @return
     */
    @Override
    public boolean isFriend(ObjectId userId, ObjectId friendId) {
        return friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent() ||
                friendRepository.findByUserIdAndFriendId(friendId, userId).isPresent();
    }

    /**
     * Hủy kết bạn
     * Kiem tra xem user va friendId co phai la ban be khong
     * Kiem tra 2 luồng userId va friendId
     * @param token user
     * @param friendId friendId
     * @return
     */
    @Override
    public Friend unfriend(String token,ObjectId friendId) {
        UserResponse user = userService.getCurrentUser(token);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        System.out.println("User: " + user.getId());
        System.out.println("FriendId: " + friendId);

        // Kiem tra xem user va friendId co phai la ban be khong
        if(isFriend(user.getId(), friendId)) {
            // Tim kiem ban be tuong ung voi userId va friendId
           Optional<Friend> friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId)
                    .or(() -> friendRepository.findByUserIdAndFriendId(friendId, user.getId()));

           // Huy ket ban
            friendRepository.delete(friend.get());
            return friend.get();
        }

        return null;
    }
}
