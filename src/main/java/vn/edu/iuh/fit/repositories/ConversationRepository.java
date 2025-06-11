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
    @Query("{ 'is_group': :#{#isGroup}, 'memberId': { $in: ?0 } }")
    List<Conversation> findOneToOneConversationByMemberIds(Set<ObjectId> memberIds, boolean isGroup);

    @Query("{ 'is_group': :#{#isGroup}, 'memberId': { $all: ?0, $size: 2 } }")
    Conversation findOneToOneConversationByTwoMemberIds(Set<ObjectId> memberIds, boolean isGroup);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'isGroup': ?1}")
    List<Conversation> findByNameAndIsGroup(String name, boolean isGroup);
    @Query("{'is_group': ?0}")
    List<Conversation> findByIsGroup(boolean isGroup);
    @Query(value = "{'name': {$regex: ?0, $options: 'i'}, 'isGroup': ?1}", exists = true)
    boolean existsByNameAndIsGroup(String name, boolean isGroup);

    // tìm idconversation theo link group
    // tìm idconversation theo link group
    @Query("{'linkGroup': ?0}")
    Optional<Conversation> findByLinkGroup(String linkGroup);

    List<Conversation> findByIdInAndIsGroupTrue(List<ObjectId> ids);
}
