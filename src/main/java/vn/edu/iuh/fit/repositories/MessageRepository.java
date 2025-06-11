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
import vn.edu.iuh.fit.entities.File;
import vn.edu.iuh.fit.entities.Message;

import java.util.List;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   19/03/2025
 * @version:    1.0
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, ObjectId> {
    List<Message> findByConversationIdOrderByTimestampAsc(ObjectId conversationId);
    List<String> findDistinctSenderIdsByConversationId(String conversationId);

    @Query("{ '_id': { $in: ?0 } }")
    List<Message> findByIdIn(Set<ObjectId> ids);

    void deleteAllByConversationId(ObjectId conversationId);

    @Query("{ 'conversationId': ?0, 'pinned': true }")
    List<Message> findPinnedMessagesByConversationId(ObjectId conversationId);
}
