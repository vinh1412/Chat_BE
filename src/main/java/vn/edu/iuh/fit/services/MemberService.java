/*
 * @ {#} MemberService.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.enums.MemberRoles;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
public interface MemberService {
    List<Member> findByUserId(ObjectId userId);
    List<Member> findMembersByConversationId(ObjectId conversationId);
    Member findByConversationIdAndUserId(ObjectId conversationId, ObjectId userId);
    void deleteByConversationId(ObjectId conversationId);
    boolean existsByConversationIdAndUserId(ObjectId conversationId, ObjectId userId);
    List<Member> findByConversationId(ObjectId conversationId);
    MemberRoles getUserRoleInConversation(ObjectId userId, ObjectId conversationId);

}
