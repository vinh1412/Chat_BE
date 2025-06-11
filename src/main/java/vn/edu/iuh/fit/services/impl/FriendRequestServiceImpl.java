/**
 * @ (#) FriendRequestServiceImpl.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.FriendRequestReq;
import vn.edu.iuh.fit.dtos.response.FriendRequestDto;
import vn.edu.iuh.fit.dtos.response.FriendRequestResponse;
import vn.edu.iuh.fit.dtos.response.FriendResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Friend;
import vn.edu.iuh.fit.entities.FriendRequest;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.FriendStatus;
import vn.edu.iuh.fit.enums.RequestFriendStatus;
import vn.edu.iuh.fit.exceptions.FriendRequestException;
import vn.edu.iuh.fit.exceptions.UserNotFoundException;
import vn.edu.iuh.fit.repositories.FriendRepository;
import vn.edu.iuh.fit.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.FriendRequestService;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@Service
public class FriendRequestServiceImpl implements FriendRequestService
{
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendService  friendService;

    //entity to dto
    private FriendRequestDto convertToDto(FriendRequest friendRequest) {
        return modelMapper.map(friendRequest, FriendRequestDto.class);
    }

    //dto to entity
    private FriendRequest convertToEntity(FriendRequestDto friendRequestReq) {
        return modelMapper.map(friendRequestReq, FriendRequest.class);
    }

    /**
     * Gửi lời mời kết bạn
     * @param token token user gui
     * @param friendRequestReq thong tin nguoi nhan
     * @return
     */
    @Override
    public FriendRequestDto sendFriendRequest(String token,FriendRequestReq friendRequestReq) {

        // lay thong tin ng gui
        UserResponse user = userService.getCurrentUser(token);

        // Validate the request
        ObjectId senderId = user.getId();
        ObjectId receiverId = friendRequestReq.getReceiverId();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("Người gửi không tồn tại."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("Người nhận không tồn tại."));

        // Kiểm tra xem người gửi và người nhận có phải là cùng một người không
        // Đồng thời kiem tra xem lời mời đã tồn tại hay chưa
        if(friendRequestRepository.existsBySenderAndReceiver(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Lời mời đã được gửi trước đó.");
        }

        // tao 1 friend request
        FriendRequest newFriendRequest = new FriendRequest();
        newFriendRequest.setSender(sender.getId());
        newFriendRequest.setReceiver(receiver.getId());
        newFriendRequest.setStatus(RequestFriendStatus.PENDING);
        newFriendRequest.setSendAt(Instant.now());

        // Lưu vào cơ sở dữ liệu
        newFriendRequest = friendRequestRepository.save(newFriendRequest);

        return this.convertToDto(newFriendRequest);

    }

    /**
     * Chấp nhận lời mời kết bạn
     * @param token token nguoi nhan
     * @param requestId id yeu cau
     * @return
     */
    @Override
    public Friend acceptFriendRequest(String token, ObjectId requestId) {
        UserResponse user = userService.getCurrentUser(token);
        if(user == null) {
            throw new FriendRequestException("Người nhận không tồn tại.");
        }

        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestException("Lời mời không tồn tại."));

        // Kiểm tra trạng thái của lời mời
        if(friendRequest.getStatus() != RequestFriendStatus.PENDING) {
            throw new FriendRequestException("Lời mời đã được chấp nhận hoặc từ chối trước đó.");
        }

        // Kiểm tra xem người dùng có phải là người nhận không
        if(!friendRequest.getReceiver().equals(user.getId())) {
            throw new FriendRequestException("Bạn không có lời mời này.");
        }

        if(friendService.isFriend(friendRequest.getReceiver(), friendRequest.getSender())) {
            throw new FriendRequestException("Đã là bạn bè.");
        }

//        friendRequest.setStatus(RequestFriendStatus.ACCEPTED);
//        friendRequest.setSendAt(Instant.now());

        // Lưu vào cơ sở dữ liệu
        friendRequestRepository.delete(friendRequest);

        Friend friend = Friend.builder()
                .userId(friendRequest.getReceiver())
                .friendId(friendRequest.getSender())
                .status(FriendStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

            friend = friendRepository.save(friend);
        // Lưu vào cơ sở dữ liệu
        return friend;
    }

    /**
     * Từ chối lời mời kết bạn
     * @param token
     * @param requestId
     * @return
     */
    @Override
    public boolean rejectFriendRequest(String token, ObjectId requestId) {
        UserResponse user = userService.getCurrentUser(token);

        System.out.println("User: " + user.getId());
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestException("Lời mời không tồn tại."));

        // Kiểm tra trạng thái của lời mời
        if(friendRequest.getStatus() != RequestFriendStatus.PENDING) {
            throw new FriendRequestException("Lời mời đã được chấp nhận hoặc từ chối trước đó.");
        }

        // Kiểm tra xem người dùng có phải là người nhận không
        if(!friendRequest.getReceiver().equals(user.getId())) {
            throw new FriendRequestException("Bạn không có lời mời này.");
        }
        // xoa yc vào cơ sở dữ liệu
        friendRequestRepository.delete(friendRequest);

        return true;
    }


    /**
     * Lấy danh sách lời mời kết bạn
     * @param token token nguoi nhan
     * @return
     */
    @Override
    public List<FriendRequestResponse> getFriendRequests(String token) {
        UserResponse user = userService.getCurrentUser(token);
        if(user == null) {
            throw new FriendRequestException("Người nhận không tồn tại.");
        }

        // Lấy danh sách lời mời kết bạn
        List<FriendRequest> friendRequests = friendRequestRepository.findByReceiverAndStatus(user.getId(), RequestFriendStatus.PENDING);
        if(friendRequests.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy danh sách người dùng từ danh sách friendRequests
        return friendRequests.stream()
                .map(friendReq -> {
                    // Lấy thông tin người gửi
                    User userSender = userRepository.findById(friendReq.getSender())
                            .orElseThrow(() -> new UserNotFoundException("Người gửi không tồn tại."));

                    FriendRequestResponse friendRequestResponse = new FriendRequestResponse();
                    friendRequestResponse.setRequestId(friendReq.getId());
                    friendRequestResponse.setUserId(userSender.getId()); // userid nguoi gui
                    friendRequestResponse.setAvatar(userSender.getAvatar());
                    friendRequestResponse.setDisplayName(userSender.getDisplayName());
                    return friendRequestResponse;

                }).toList();
    }

    /**
     * Lấy danh sách lời mời đã gửi
     * @param token token nguoi gui
     * @return
     */
    @Override
    public List<FriendRequestResponse> getSentFriendRequests(String token) {

        UserResponse user = userService.getCurrentUser(token);
        if(user == null) {
            throw new FriendRequestException("Người dung không tồn tại.");
        }

        // Lấy danh sách lời mời đã gửi
        List<FriendRequest> friendRequests = friendRequestRepository.findBySenderAndStatus(user.getId(), RequestFriendStatus.PENDING);

        if(friendRequests.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy danh sách người dùng đã gửi từ danh sách requests
        return friendRequests.stream()
                .map(requested -> {

                    // Lấy thông tin người nhận
                    User userReceiver = userRepository.findById(requested.getReceiver())
                            .orElseThrow(() -> new UserNotFoundException("Người nhận không tồn tại."));

                    FriendRequestResponse response = new FriendRequestResponse();
                    response.setRequestId(requested.getId());
                    response.setUserId(userReceiver.getId());
                    response.setAvatar(userReceiver.getAvatar());
                    response.setDisplayName(userReceiver.getDisplayName());
                    return response;
                }).collect(Collectors.toList());
    }


    /**
     * Thu hồi lời mời kết bạn đã gửi
     * @param token token nguoi gui
     * @param requestId id yeu cau
     * @return
     */
    @Override
    public boolean  recallFriendRequestSent(String token, ObjectId requestId) {
        UserResponse user = userService.getCurrentUser(token);
        if(user == null) {
            throw new FriendRequestException("Người dung không tồn tại.");
        }

        // Tìm lời mời kết bạn đã gửi
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestException("Lời mời không tồn tại."));

        // Kiểm tra xem người dùng có phải là người gửi không
        if (!request.getSender().equals(user.getId())) {
            throw new FriendRequestException("Bạn không có quyền thu hồi lời mời này.");
        }

        // Kiểm tra trạng thái của lời mời
        if(request.getStatus() != RequestFriendStatus.PENDING) {
            throw new FriendRequestException("Lời mời đã được chấp nhận hoặc từ chối trước đó.");
        }

        // Xóa lời mời kết bạn
        friendRequestRepository.delete(request);
        return true;
    }
}
