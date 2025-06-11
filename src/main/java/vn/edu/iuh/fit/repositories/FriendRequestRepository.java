/*
 * @ {#} AttachmentRepository.java   1.0     19/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Friend;
import vn.edu.iuh.fit.entities.FriendRequest;
import vn.edu.iuh.fit.enums.RequestFriendStatus;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   19/03/2025
 * @version:    1.0
 */
@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, ObjectId> {
    boolean existsBySenderAndReceiver(ObjectId sender, ObjectId receiver);

    // Tìm danh sách lời mời đã nhận (Pending)
    List<FriendRequest> findByReceiverAndStatus(ObjectId receiver, RequestFriendStatus status);

    // Tìm danh sách lời mời đã gửi (Pending)
    List<FriendRequest> findBySenderAndStatus(ObjectId sender, RequestFriendStatus status);

    // Tim loi mời bạn bè đã gửi



}
