/**
 * @ (#) FriendController.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.FriendRequestReq;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.Friend;
import vn.edu.iuh.fit.entities.FriendRequest;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.FriendRepository;
import vn.edu.iuh.fit.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.FriendRequestService;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.util.List;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@RestController
@RequestMapping("/api/v1/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;
    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/friend-requests/received")
    public ResponseEntity<ApiResponse<?>> getFriendsRequest(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Token: " + token);
            List<FriendRequestResponse>  friendRequests = friendRequestService.getFriendRequests(token);
            if(friendRequests.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request  is empty").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request  successfully").response(friendRequests).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<ApiResponse<?>> getSentFriendsRequest(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Token: " + token);
            List<FriendRequestResponse>  friendRequests = friendRequestService.getSentFriendRequests(token);

            if(friendRequests.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request Sent is empty").build());
            }

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request Sent successfully").response(friendRequests).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    /**
     * api send friend request
     * @param token
     * @param friendRequestReq
     * @return
     */
    @PostMapping("/send-request")
    public ResponseEntity<ApiResponse<?>> sendFriendRequest(@RequestHeader("Authorization") String token, @RequestBody FriendRequestReq friendRequestReq) {
        try {
            FriendRequestDto response = friendRequestService.sendFriendRequest(token,friendRequestReq);

            User userReceiver = userRepository.findById(response.getSender())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            FriendRequestResponse friendReceived = new FriendRequestResponse();
            friendReceived.setUserId(userReceiver.getId());
            friendReceived.setDisplayName(userReceiver.getDisplayName());
            friendReceived.setAvatar(userReceiver.getAvatar());
            friendReceived.setRequestId(response.getId());

            simpMessagingTemplate.convertAndSend("/friend/request/" + response.getReceiver(), friendReceived);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Request Friend Successfully").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @MessageMapping("/friend/send-request")
    public ResponseEntity<ApiResponse<?>> sendFriendRequest_Socket(@Header("Authorization") String token, FriendRequestReq friendRequestReq) {
        try {
            FriendRequestDto response = friendRequestService.sendFriendRequest(token,friendRequestReq);

            User userReceiver = userRepository.findById(response.getSender())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            FriendRequestResponse friendReceived = new FriendRequestResponse();
            friendReceived.setUserId(userReceiver.getId());
            friendReceived.setDisplayName(userReceiver.getDisplayName());
            friendReceived.setAvatar(userReceiver.getAvatar());
            friendReceived.setRequestId(response.getId());

            simpMessagingTemplate.convertAndSend("/friend/request/" + response.getReceiver(), friendReceived);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Request Friend Successfully").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/accept-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> acceptFriendRequest(@RequestHeader("Authorization") String token,
                                                               @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            Friend isAccepted = friendRequestService.acceptFriendRequest(token, requestId);
            if(isAccepted == null) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to accept friend request").build());
            }
           User friendRequest = userRepository.findById(isAccepted.getFriendId())
                    .orElseThrow(() -> new RuntimeException("Friend not found"));
            User user = userRepository.findById(isAccepted.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            FriendResponse friendReceived = new FriendResponse();
            friendReceived.setUserId(user.getId());
            friendReceived.setDisplayName(user.getDisplayName());
            friendReceived.setAvatar(user.getAvatar());

            FriendResponse friendSent= new FriendResponse();
            friendSent.setUserId(friendRequest.getId());
            friendSent.setDisplayName(friendRequest.getDisplayName());
            friendSent.setAvatar(friendRequest.getAvatar());

            simpMessagingTemplate.convertAndSend("/friend/accept/" + friendRequest.getId(), friendReceived);
            simpMessagingTemplate.convertAndSend("/friend/accept/" + user.getId(), friendSent);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Accept Friend Request Successfully").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/reject-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> rejectFriendRequest(@RequestHeader("Authorization") String token,
                                                              @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            boolean isAccepted = friendRequestService.rejectFriendRequest(token, requestId);
            if(!isAccepted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to reject friend request").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Reject Friend Request Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/recall-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> recallFriendRequest(@RequestHeader("Authorization") String token,
                                                              @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            boolean isAccepted = friendRequestService.recallFriendRequestSent(token, requestId);
            if(!isAccepted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to recall friend request").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Recall Friend Request Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/unfriend/{friendId}")
    public ResponseEntity<ApiResponse<?>> unfriend(@RequestHeader("Authorization") String token,
                                                   @PathVariable("friendId") ObjectId friendId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Friend ID: " + friendId);
            Friend isUnfriend = friendService.unfriend(token, friendId);
            if(isUnfriend == null) {
                return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                        .body(ApiResponse.builder().status("FAILED").message("Failed to unfriend").build());
            }

            User friend = userRepository.findById(isUnfriend.getFriendId())
                    .orElseThrow(() -> new RuntimeException("Friend not found"));
            User user = userRepository.findById(isUnfriend.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            FriendResponse inf_User = new FriendResponse();
            inf_User.setUserId(user.getId());
            inf_User.setDisplayName(user.getDisplayName());
            inf_User.setAvatar(user.getAvatar());

            FriendResponse inf_Friend = new FriendResponse();
            inf_Friend.setUserId(friend.getId());
            inf_Friend.setDisplayName(friend.getDisplayName());
            inf_Friend.setAvatar(friend.getAvatar());

            simpMessagingTemplate.convertAndSend("/friend/unfriend/" + friend.getId(), inf_User);
            simpMessagingTemplate.convertAndSend("/friend/unfriend/" + user.getId(), inf_Friend);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Unfriend Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @GetMapping("/check-friend")
    public ResponseEntity<ApiResponse<?>> checkFriend(@RequestHeader("Authorization") String token,
                                                   @RequestParam ObjectId friendId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Friend ID: " + friendId);
            UserResponse user = userService.getCurrentUser(token);
            if(user == null) {
                return ResponseEntity.noContent().build();
            }
            boolean isFriend = friendService.isFriend(user.getId(), friendId);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Check friend Successfully").response(isFriend).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    /*
     Lấy danh sách bạn be của người dùng
     */
    @GetMapping("/my-friends/{id}")
    public ResponseEntity<ApiResponse<?>> getMyFriends(@PathVariable("id") ObjectId id) {
        try {
            List<FriendResponse> friends = friendService.getFriends(id);
            if(friends.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.builder().status("SUCCESS").message("Get list Friend is empty").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get list Friend successfully").response(friends).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }


}
