/*
 * @ {#} MemberServiceImpl.java   1.0     23/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.services.MemberService;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   23/04/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public List<Member> findByUserId(ObjectId userId) {
        return List.of();
    }

    @Override
    public List<Member> findMembersByConversationId(ObjectId conversationId) {
        return List.of();
    }

    @Override
    public Member findByConversationIdAndUserId(ObjectId conversationId, ObjectId userId) {
        return null;
    }

    @Override
    public void deleteByConversationId(ObjectId conversationId) {

    }

    @Override
    public boolean existsByConversationIdAndUserId(ObjectId conversationId, ObjectId userId) {
        return false;
    }

    @Override
    public List<Member> findByConversationId(ObjectId conversationId) {
        return List.of();
    }

    @Override
    public MemberRoles getUserRoleInConversation(ObjectId userId, ObjectId conversationId) {
        Member member = memberRepository.findByUserIdAndConversationId(userId, conversationId);

        if (member == null) {
            throw new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện");
        }

        return member.getRole();
    }
}
