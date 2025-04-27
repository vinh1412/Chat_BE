/*
 * @ {#} AttachmentRepository.java   1.0     19/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.File;
import vn.edu.iuh.fit.entities.Member;

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   19/03/2025
 * @version:    1.0
 */
@Repository
public interface MemberRepository extends MongoRepository<Member, ObjectId> {
    List<Member> findByUserId(ObjectId userId);
    List<Member> findByConversationId(ObjectId conversationId);
    Member findByUserIdAndConversationId(ObjectId userId, ObjectId conversationId);
    void deleteByUserIdAndConversationId(ObjectId userId, ObjectId conversationId);
    List<Member> findAllByConversationId(ObjectId conversationId);

    Optional<Member> findByConversationIdAndUserId(ObjectId conversationId, ObjectId userId);

    void deleteByConversationIdAndUserId(ObjectId conversationId, ObjectId userId);
}
