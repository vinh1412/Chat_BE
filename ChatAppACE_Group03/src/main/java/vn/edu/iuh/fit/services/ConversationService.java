/*
 * @ {#} ConservationService.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.ConversationDTO;

import java.util.List;

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
}
