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

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   19/03/2025
 * @version:    1.0
 */
@Repository
public interface FriendRepository extends MongoRepository<Friend, ObjectId> {
    List<Friend> findByUserId(ObjectId userId);
    List<Friend> findByFriendId(ObjectId userId);

    // Kiểm tra mối quan hệ bạn bè giữa userId1 và userId2
    Optional<Friend> findByUserIdAndFriendId(ObjectId userId, ObjectId friendId);
}
