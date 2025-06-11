/**
 * @ (#) FriendRequestService.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.request.FriendRequestReq;
import vn.edu.iuh.fit.dtos.response.FriendRequestDto;
import vn.edu.iuh.fit.dtos.response.FriendRequestResponse;
import vn.edu.iuh.fit.dtos.response.FriendResponse;
import vn.edu.iuh.fit.entities.Friend;

import java.util.List;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
public interface FriendRequestService {
    public FriendRequestDto sendFriendRequest(String token,FriendRequestReq friendRequestReq);
    public Friend acceptFriendRequest(String token, ObjectId requestId);
    public boolean rejectFriendRequest(String token, ObjectId requestId);

    List<FriendRequestResponse> getFriendRequests(String token);
    public List<FriendRequestResponse> getSentFriendRequests(String token);

    public boolean  recallFriendRequestSent(String token, ObjectId requestId);
}
