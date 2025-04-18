/*
 * @ {#} AttachmentRepository.java   1.0     19/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   19/03/2025
 * @version:    1.0
 */
@Repository
public interface ConversationRepository extends MongoRepository<Conversation, ObjectId> {
    @Query("{ 'group': :#{#isGroup}, '_id': { $in: ?0 } }")
    List<Conversation> findOneToOneConversationByMemberIds(Set<ObjectId> memberIds, boolean isGroup);

}
