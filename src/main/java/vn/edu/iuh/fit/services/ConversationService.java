/*
 * @ {#} ConservationService.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.response.MemberResponse;
import vn.edu.iuh.fit.entities.Message;

import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
public interface ConversationService {
    // Hàm tạo mới một cuộc trò chuyện (conversation) và gán các thành viên vào cuộc trò chuyện đó
    ConversationDTO createConversationOneToOne(ConversationDTO conversationDTO);
    // Hàm tạo cuộc trò chuyện nhóm
    ConversationDTO createConversationGroup(ObjectId creatorId, ConversationDTO conversationDTO);
    // Hàm tìm kiếm cuộc trò chuyện theo id
    ConversationDTO findConversationById(ObjectId conversationId);
    // Hàm tìm kiếm tất cả cuộc trò chuyện của người dùng theo id
    List<ConversationDTO> findAllConversationsByUserId(ObjectId userId);
    // Hàm tìm kiếm Id cuộc trò chuyện cuối cùng trong danh sách Id cuộc trò chuyện
    ObjectId getLastMessageId(Set<ObjectId> messageIds);
    // Hàm tìm hoặc tạo cuộc trò chuyện giữa người gửi và người nhận. Nếu chưa có cuộc trò chuyện, tạo mới.
    ConversationDTO findOrCreateConversation(ObjectId senderId, String receiverId);
    ConversationDTO findConversationByMembers(ObjectId senderId, ObjectId receiverId);

    void addPinnedMessage(ObjectId conversationId, ObjectId messageId);
    void removePinnedMessage(ObjectId conversationId, ObjectId messageId);
    boolean isMember(ObjectId conversationId, ObjectId userId);

    Map<String, Object> dissolveGroup(ObjectId conversationId, ObjectId userId);
    public Message leaveGroup(ObjectId conversationId, String token);
    public Message removeGroup(ObjectId conversationId, String token, ObjectId userId);
    public Message addMemberGroup(ObjectId conversationId, ObjectId userId);
    public List<MemberResponse> findUserByIDConversation(ObjectId conversationId);

    //Haàm cập nhật role thành viên trong nhóm
    ConversationDTO updateMemberRole(ObjectId conversationId, ObjectId memberId, String newRole, ObjectId requestingUserId);

    ConversationDTO deleteConversationForUser(ObjectId conversationId, ObjectId userId);

    // UPDATE TÊN NHÓM
    ConversationDTO updateGroupName(ObjectId conversationId, String newGroupName);

    //  tìm linkGroup theo ConversationId
    String findLinkGroupByConversationId(ObjectId conversationId);

    //tim conversationId theo linkGroup
    ConversationDTO findConversationIdByLinkGroup(String linkGroup);

    // Find all group conversations for a user
    List<ConversationDTO> findAllGroupConversationsByUserId(ObjectId userId);
}
