/*
 * @ {#} ConservationServiceImpl.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.model.ResourceNotFoundException;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.response.MemberResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Conversation;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.exceptions.ConversationNotFoundException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.repositories.ConversationRepository;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private ConversationDTO mapToDTO(Conversation conversation) {
        ConversationDTO dto = ConversationDTO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .avatar(conversation.getAvatar())
                .isGroup(conversation.isGroup())
                .lastMessageId(conversation.getLastMessageId())
                .createdAt(conversation.getCreatedAt())
                .memberId(conversation.getMemberId())
                .messageIds(conversation.getMessageIds())
                .dissolved(conversation.isDissolved())
                .dissolvedBy(conversation.getDissolvedBy())
                .dissolvedAt(conversation.getDissolvedAt())
                .removedByUserIds(conversation.getRemovedByUserIds())
                .linkGroup(conversation.getLinkGroup())
                .restrictMessagingToAdmin(conversation.isRestrictMessagingToAdmin())
                .build();


        // Populate membersGroup
        List<Member> membersGroup = memberRepository.findByConversationId(conversation.getId());
        List<MemberResponse> memberResponses = membersGroup.stream().map(member -> {
            User user = userRepository.findById(member.getUserId()).orElse(null);
            if (user == null) return null;
            return MemberResponse.builder()
                    .id(user.getId())
                    .displayName(user.getDisplayName())
                    .avatar(user.getAvatar())
                    .phone(user.getPhone())
                    .role(member.getRole())
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        dto.setMembers(memberResponses);

        // Populate lastMessage
        if (conversation.getLastMessageId() != null) {
            messageRepository.findById(conversation.getLastMessageId())
                    .ifPresent(message -> {
                        dto.setLastMessage(MessageDTO.builder()
                                .id(message.getId())
                                .content(message.getContent())
                                .senderId(message.getSenderId())
                                .conversationId(message.getConversationId())
                                .timestamp(message.getTimestamp())
                                .messageType(message.getMessageType())
                                .build());
                    });
        }else {
            dto.setLastMessage(null);
        }

        return dto;
    }

    private Conversation mapToEntity(ConversationDTO conversationDTO) {
        return modelMapper.map(conversationDTO, Conversation.class);
    }


    @Override
    public ConversationDTO createConversationOneToOne(ConversationDTO conversationDTO) {
        // Lấy userId từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        System.out.println("currentUserId: " + phone);
        User currentUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy người dùng với phone: " + phone));
        System.out.println("currentUser: " + currentUser);

        Set<ObjectId> memberIds = Optional.ofNullable(conversationDTO.getMemberId())
                .orElseThrow(() -> new ConversationCreationException("memberId không được null"));

        // Thêm userId hiện tại vào danh sách memberIds
        memberIds.add(currentUser.getId());

        // Validate phải đúng 2 user và không trùng lặp
        if (memberIds.size() != 2) {
            throw new ConversationCreationException("Cuộc trò chuyện một-một phải có đúng 2 thành viên.");
        }

        if (memberIds.size() != new HashSet<>(memberIds).size()) {
            throw new ConversationCreationException("Danh sách memberId chứa giá trị trùng lặp.");
        }

        // Xác định memberId của người còn lại
        ObjectId otherMemberId = memberIds.stream()
                .filter(id -> !id.equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy memberId của người còn lại."));

        System.out.println("otherMemberId: " + otherMemberId);

        // Lấy thông tin user còn lại từ userRepository
        User otherUser = userRepository.findById(otherMemberId)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy user với ID: " + otherMemberId));

        System.out.println("otherUser: " + otherUser);

        // Kiểm tra xem đã có cuộc trò chuyện một-một giữa 2 người này chưa
        List<Conversation> existingConversations = conversationRepository.findOneToOneConversationByMemberIds(memberIds, false);
        for (Conversation existing : existingConversations) {
            Set<ObjectId> existingMemberIds = memberRepository.findByConversationId(existing.getId())
                    .stream()
                    .map(Member::getUserId)
                    .collect(Collectors.toSet());

            if (existingMemberIds.equals(memberIds)) {
                // Đã tồn tại cuộc trò chuyện một-một
                ConversationDTO existingDTO = mapToDTO(existing);
                existingDTO.setMemberId(existingMemberIds);
                existingDTO.setName(otherUser.getDisplayName());
                existingDTO.setAvatar(otherUser.getAvatar());
                return existingDTO;
            }
        }
        
        // Chưa có, tiến hành tạo mới
        Conversation conversation = mapToEntity(conversationDTO);
        conversation.setGroup(false);
        conversation.setCreatedAt(Instant.now());
        conversation.setName(otherUser.getDisplayName());
        conversation.setAvatar(otherUser.getAvatar());
        Conversation savedConversation = conversationRepository.save(conversation);

        // Tạo danh sách Member
        Set<Member> members = memberIds.stream()
                .map(memberId -> Member.builder()
                        .userId(memberId)
                        .conversationId(savedConversation.getId())
                        .role(MemberRoles.MEMBER)
                        .joinedAt(Instant.now())
                        .build())
                .collect(Collectors.toSet());

        memberRepository.saveAll(members);

        ConversationDTO result = mapToDTO(savedConversation);
        result.setMemberId(memberIds);
        result.setName(otherUser.getDisplayName());
        result.setAvatar(otherUser.getAvatar());

        // Nếu DTO đầu vào có messageIds -> gán lại và tính lastMessageId
        if (conversationDTO.getMessageIds() != null && !conversationDTO.getMessageIds().isEmpty()) {
            result.setMessageIds(conversationDTO.getMessageIds());
            ObjectId lastMessageId = getLastMessageId(result.getMessageIds());
            result.setLastMessageId(lastMessageId);
            // Populate lastMessage
            if (lastMessageId != null) {
                messageRepository.findById(lastMessageId)
                        .ifPresent(message -> {
                            MessageDTO messageDTO = MessageDTO.builder()
                                    .id(message.getId())
                                    .content(message.getContent())
                                    .senderId(message.getSenderId())
                                    .conversationId(message.getConversationId())
                                    .timestamp(message.getTimestamp())
                                    .messageType(message.getMessageType())
                                    .build();
                            result.setLastMessage(messageDTO);
                            log.debug("Populated lastMessage for new conversation {}: {}", savedConversation.getId(), messageDTO.getId());
                        });
            }
        } else {
            // Mặc định là rỗng khi vừa tạo mới
            result.setMessageIds(new HashSet<>());
            result.setLastMessageId(null);
            result.setLastMessage(null);
            log.debug("No messages for new conversation {}", savedConversation.getId());
        }
        return result;
    }

    // Tạo cuộc trò chuyện nhóm
    @Override
    @Transactional
    public ConversationDTO createConversationGroup(ObjectId creatorId, ConversationDTO conversationDTO) {
        // Gọi hàm validateGroupName để kiểm tra tên nhóm
        validateGroupName(conversationDTO.getName());

        Set<ObjectId> allMemberIds = getAllMemberIds(creatorId, conversationDTO);
        // Gọi hàm validateUserExistence để kiểm tra xem tất cả userIds có tồn tại không
        validateUserExistence(allMemberIds);

        // Gọi hàm checkIfGroupExistsWithSameMembers để kiểm tra xem đã có nhóm nào tồn tại với cùng tên và thành viên chưa
        Optional<Conversation> existingGroup = findGroupIfExists(allMemberIds, conversationDTO);
        if (existingGroup.isPresent()) {
            return buildConversationDTO(existingGroup.get(), allMemberIds); // trả về nhóm đã tồn tại
        }


        Conversation conversation = saveConversation(conversationDTO, creatorId, allMemberIds, Optional.empty());
        log.info("Created new group conversation: {}", conversation.getId());
        // Lưu danh sách thành viên vào cơ sở dữ liệu
        saveMembers(conversation, creatorId, conversationDTO.getMemberId());

        return buildConversationDTO(conversation, allMemberIds);
    }

    // Hàm kiểm tra tên nhóm trống hoặc đã tồn tại
    private void validateGroupName(String groupName) {
        // Kiểm tra tên nhóm không được null hoặc rỗng
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new ConversationCreationException("Tên nhóm không được để trống");
        }
    }

    // Hàm lấy tất cả ID thành viên bao gồm cả creatorId
    private Set<ObjectId> getAllMemberIds(ObjectId creatorId, ConversationDTO conversationDTO) {
        // Lấy danh sách ID của các thành viên từ ConversationDTO
        Set<ObjectId> memberIds = Optional.ofNullable(conversationDTO.getMemberId())
                .orElseThrow(() -> new ConversationCreationException("memberId không được null"));

        // Kiểm tra số lượng thành viên
        if (memberIds.size() < 2) {
            throw new ConversationCreationException("Nhóm trò chuyện phải có ít nhất 2 thành viên khác ngoài người tạo");
        }

        // Kiểm tra trùng lặp với creatorId
        if (memberIds.contains(creatorId)) {
            throw new ConversationCreationException("creatorId không được trùng với memberId");
        }

        Set<ObjectId> allMemberIds = new HashSet<>(memberIds);
        allMemberIds.add(creatorId);

        return allMemberIds;
    }

    // Hàm kiểm tra xem tất cả userIds có tồn tại không
    private void validateUserExistence(Set<ObjectId> allMemberIds) {
        List<User> existingUsers = userRepository.findAllById(allMemberIds);
        System.out.println("existingUsers: " + existingUsers);

        // Kiểm tra xem tất cả userIds có tồn tại không
        if (existingUsers.size() != allMemberIds.size()) {
            throw new ConversationCreationException("Một hoặc nhiều thành viên không tồn tại trong hệ thống");
        }
    }

    // Hàm kiểm tra xem đã có nhóm nào tồn tại với cùng tên và thành viên chưa
    private Optional<Conversation> findGroupIfExists(Set<ObjectId> allMemberIds, ConversationDTO conversationDTO) {
        if (conversationRepository.existsByNameAndIsGroup(conversationDTO.getName(), true)) {
            for (Conversation existingGroup : conversationRepository.findByNameAndIsGroup(conversationDTO.getName(), true)) {
                Set<ObjectId> existingMemberIds = memberRepository.findByConversationId(existingGroup.getId()).stream()
                        .map(Member::getUserId)
                        .collect(Collectors.toSet());

                if (existingMemberIds.equals(allMemberIds)) {
                    log.info("Found existing group with same name and members: {}", existingGroup.getId());
                    return Optional.of(existingGroup);
                }
            }
        }
        return Optional.empty();
    }

    // Hàm lưu cuộc trò chuyện vào cơ sở dữ liệu
    private Conversation saveConversation(ConversationDTO conversationDTO, ObjectId creatorId, Set<ObjectId> allMemberIds, Optional<Conversation> existingGroup) {
        Conversation conversation = mapToEntity(conversationDTO);
        conversation.setName(conversationDTO.getName());
        conversation.setGroup(true);
        conversation.setCreatedAt(existingGroup.map(Conversation::getCreatedAt).orElse(Instant.now()));
        conversation.setMemberId(allMemberIds);

        return conversationRepository.save(conversation);
    }

    // Hàm lưu danh sách thành viên vào cơ sở dữ liệu
    private void saveMembers(Conversation conversation, ObjectId creatorId, Set<ObjectId> memberIds) {
        Set<Member> members = new HashSet<>();

        members.add(Member.builder()
                .conversationId(conversation.getId())
                .userId(creatorId)
                .role(MemberRoles.ADMIN)
                .joinedAt(Instant.now())
                .build());

        members.addAll(memberIds.stream()
                .map(memberId -> Member.builder()
                        .conversationId(conversation.getId())
                        .userId(memberId)
                        .role(MemberRoles.MEMBER)
                        .joinedAt(Instant.now())
                        .build())
                .collect(Collectors.toSet()));

        memberRepository.saveAll(members);
    }

    // Hàm xây dựng ConversationDTO từ Conversation đã lưu
    private ConversationDTO buildConversationDTO(Conversation savedConversation, Set<ObjectId> allMemberIds) {
        ConversationDTO result = mapToDTO(savedConversation);
        result.setMemberId(allMemberIds);
        result.setMessageIds(new HashSet<>());
        result.setLastMessageId(null);
        result.setLastMessage(null);

        List<MemberResponse> memberResponses = memberRepository.findByConversationId(savedConversation.getId()).stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId()).orElse(null);
                    return user == null ? null : MemberResponse.builder()
                            .id(user.getId())
                            .displayName(user.getDisplayName())
                            .avatar(user.getAvatar())
                            .phone(user.getPhone())
                            .role(member.getRole())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        result.setMembers(memberResponses);

        return result;
    }

    private MemberResponse toMemberResponse(UserResponse userResponse, MemberRoles role) {
        return MemberResponse.builder()
                .id(userResponse.getId())
                .displayName(userResponse.getDisplayName())
                .avatar(userResponse.getAvatar())
                .phone(userResponse.getPhone())
                .role(role)
                .build();
    }

//    @Transactional(readOnly = true) // Đánh dấu phương thức này là chỉ đọc để tối ưu hóa hiệu suất
    @Override
    public ConversationDTO findConversationById(ObjectId conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToDTO(conversation);
    }


    // Tìm tất cả các cuộc trò chuyện của người dùng
    @Transactional(readOnly = true)
    @Override
    public List<ConversationDTO> findAllConversationsByUserId(ObjectId userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId không được null");
        }

        // Tìm tất cả các thành viên người dùng tham gia
        List<Member> members = memberRepository.findByUserId(userId);
        if (members.isEmpty()) {
            log.debug("No conversations found for userId: {}", userId);
            return Collections.emptyList();
        }

        // Lấy danh sách ID của các cuộc trò chuyện mà người dùng tham gia
        List<ObjectId> conversationIds = members.stream()
                .map(Member::getConversationId)
                .collect(Collectors.toList());

        // Tìm tất cả các cuộc trò chuyện dựa trên danh sách ID
        List<Conversation> conversations = conversationRepository.findAllById(conversationIds);

        return conversations.stream().map(conversation -> {
            ConversationDTO dto = mapToDTO(conversation);

            // Lấy tất cả các thành viên của cuộc trò chuyện
            List<Member> allMembers = memberRepository.findByConversationId(conversation.getId());

            // Map userId <-> role
            Map<ObjectId, MemberRoles> roleMap = allMembers.stream()
                    .collect(Collectors.toMap(Member::getUserId, Member::getRole));

            Set<ObjectId> memberIds = roleMap.keySet();
            dto.setMemberId(memberIds);

            // Lấy thông tin user
            List<UserResponse> userResponses = userService.getUsersByIds(memberIds);

            // Chuyển sang MemberResponse
            List<MemberResponse> memberResponses = userResponses.stream()
                    .map(user -> toMemberResponse(user, roleMap.get(user.getId())))
                    .collect(Collectors.toList());

            dto.setMembers(memberResponses);

            // Gán lastMessage nếu có
            if (conversation.getLastMessageId() != null) {
                Optional<Message> lastMessageOpt = messageRepository.findById(conversation.getLastMessageId());
                lastMessageOpt.ifPresent(message -> {
                    MessageDTO messageDTO = modelMapper.map(message, MessageDTO.class);
                    dto.setLastMessage(messageDTO);
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ObjectId getLastMessageId(Set<ObjectId> messageIds) {
        return messageRepository.findByIdIn(messageIds).stream()
                .max(Comparator.comparing(Message::getTimestamp))
                .map(Message::getId)
                .orElse(null);
    }


    // Tìm hoặc tạo cuộc trò chuyện giữa người gửi và người nhận
    @Override
    public ConversationDTO findOrCreateConversation(ObjectId senderId, String receiverId) {
        log.debug("Finding or creating conversation for sender: {} and receiver: {}", senderId, receiverId);

        // Kiểm tra xem receiverId là userId hay groupId
        boolean isGroup = receiverId.startsWith("group_");
        if (isGroup) {
            try {
                ObjectId groupId = new ObjectId(receiverId.replace("group_", ""));
                ConversationDTO groupConversation = findConversationById(groupId);
                if (groupConversation != null) {
                    log.info("Found group conversation with ID: {}", groupId);
                    return groupConversation;
                }
                throw new IllegalArgumentException("Group not found with ID: " + groupId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid groupId format: {}", receiverId);
                throw new IllegalArgumentException("Invalid groupId format: " + receiverId);
            }
        }

        // Xử lý cuộc trò chuyện 1-1
        ObjectId userId;
        try {
            userId = new ObjectId(receiverId);
            System.out.println("receiverId Id: " + userId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid userId format: {}", receiverId);
            throw new IllegalArgumentException("Invalid userId format: " + receiverId);
        }

        // Kiểm tra xem userId có tồn tại trong hệ thống không
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        System.out.println("receiver user: " + receiver);
        System.out.println("sednerid : " + senderId);
        // Tìm cuộc trò chuyện 1-1
        ConversationDTO existingConversation = findConversationByMembers(senderId, receiver.getId());
        System.out.println("existingConversation: " + existingConversation);
        if (existingConversation != null) {
            log.info("Found existing one-to-one conversation: {}", existingConversation.getId());
            return existingConversation;
        }

        // Tạo cuộc trò chuyện mới nếu không tồn tại
        log.info("Creating new one-to-one conversation between {} and {}", senderId, userId);
        ConversationDTO newConversation = new ConversationDTO();
        newConversation.setMemberId(new HashSet<>(Arrays.asList(senderId, receiver.getId())));
        newConversation.setGroup(false);
        newConversation.setName(receiver.getDisplayName()); // Đặt tên mặc định là tên người nhận
        newConversation.setAvatar(receiver.getAvatar()); // Đặt avatar mặc định
        newConversation.setCreatedAt(Instant.now());
        newConversation.setMessageIds(new HashSet<>());
        newConversation.setLastMessageId(null);
        newConversation.setLastMessage(null);

        return createConversationOneToOne(newConversation);
    }

    @Override
    public ConversationDTO findConversationByMembers(ObjectId senderId, ObjectId receiverId) {
        Conversation conversation = conversationRepository.findOneToOneConversationByTwoMemberIds(
                new HashSet<>(Arrays.asList(senderId, receiverId)), false);
        if (conversation == null) {
            log.info("No conversation found between sender: {} and receiver: {}", senderId, receiverId);
            return null;
        }
        System.out.println("conversation: " + conversation);
       return this.mapToDTO(conversation);
    }

    @Override
    public void addPinnedMessage(ObjectId conversationId, ObjectId messageId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        List<ObjectId> pinnedMessages = conversation.getPinnedMessages();
        if (!pinnedMessages.contains(messageId)) {
            pinnedMessages.add(messageId);
            conversation.setPinnedMessages(pinnedMessages);
            conversationRepository.save(conversation);
        }
    }

    @Override
    public void removePinnedMessage(ObjectId conversationId, ObjectId messageId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        List<ObjectId> pinnedMessages = conversation.getPinnedMessages();
        pinnedMessages.remove(messageId);
        conversation.setPinnedMessages(pinnedMessages);
        conversationRepository.save(conversation);
    }

    @Override
    public boolean isMember(ObjectId conversationId, ObjectId userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        return conversation.getMemberId().contains(userId);
    }

    @Override
    public Map<String, Object> dissolveGroup(ObjectId conversationId, ObjectId userId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

        // Kiểm tra xem cuộc trò chuyện có tồn tại không
        if (!conversationOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy nhóm trò chuyện");
        }

        Conversation conversation = conversationOpt.get();

        // Kiểm tra xem đây có phải là nhóm không
        if (!conversation.isGroup()) {
            throw new RuntimeException("Chỉ có thể giải tán các cuộc trò chuyện nhóm");
        }

        Member userMember = memberRepository.findByUserIdAndConversationId(userId, conversationId);

        // Kiểm tra xem người dùng có quyền giải tán nhóm không
        if (userMember == null || (userMember.getRole() != MemberRoles.ADMIN)) {
            return Map.of("success", false, "message", "Không có quyền giải tán nhóm");
        }

        // Đánh dấu nhóm đã giải tán
        conversation.setDissolved(true);
        conversation.setDissolvedBy(userId);
        conversation.setDissolvedAt(Instant.now());
        conversation.getMemberId().remove(userId);
        conversationRepository.save(conversation);

        //Xóa tất cả tin nhắn trong nhóm
        messageRepository.deleteAllByConversationId(conversationId);

        // Xóa conversation khỏi danh sách của trưởng nhóm
        memberRepository.deleteByUserIdAndConversationId(userId, conversationId);

        //Lấy danh sách thành viên còn lại để gửi thông báo
        List<Member> members = memberRepository.findAllByConversationId(conversationId);

        return Map.of(
                "success", true,
                "conversationId", conversationId,
                "dissolvedBy", userId,
                "members", members
        );
    }

    public Message leaveGroup(ObjectId conversationId, String token) {
        // Lấy thông tin người dùng từ token
        UserResponse user = userService.getCurrentUser(token);
        if(user == null) {
            throw new ConversationCreationException("Người dùng không tồn tại");
        }

        // Tìm cuộc trò chuyện theo ID
        ConversationDTO conversationDTO = this.findConversationById(conversationId);
        if(conversationDTO == null) {
            throw new ConversationCreationException("Cuộc trò chuyện không tồn tại");
        }

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if(!conversationDTO.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        // Kiểm tra xem người dùng có phải là thành viên của cuộc trò chuyện không
        if(!this.isMember(conversationId, user.getId())) {
            throw new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện");
        }

        //Kiểm tra xem người dùng có phải là admin không
        Member member  = memberRepository.findByConversationIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        // Nếu người dùng là admin thì gan admin cho người khác
        if (member.getRole() == MemberRoles.ADMIN) {
            List<Member> remainingMembers = memberRepository.findByConversationId(conversationId).stream()
                    .filter(m -> !m.getUserId().equals(user.getId()))
                    .collect(Collectors.toList());

            if (remainingMembers.isEmpty()) {
                // Nếu không còn thành viên nào khác, giải tán nhóm
                Map<String, Object> dissolveResult = dissolveGroup(conversationId, user.getId());
                if (!(boolean) dissolveResult.get("success")) {
                    throw new ConversationCreationException("Failed to dissolve group: " + dissolveResult.get("message"));
                }
                Message message = Message.builder()
                        .conversationId(conversationDTO.getId())
                        .messageType(MessageType.SYSTEM)
                        .content("Nhóm đã được giải tán vì không còn thành viên")
                        .timestamp(Instant.now())
                        .recalled(false)
                        .build();
                return messageRepository.save(message);
            }

            // Chọn một thành viên còn lại để trở thành admin
            Member newAdmin = remainingMembers.stream()
                    .filter(m -> m.getRole() == MemberRoles.DEPUTY)
                    .findFirst()
                    .orElse(remainingMembers.get(0)); // Nếu không có deputy, chọn thành viên đầu tiên

            newAdmin.setRole(MemberRoles.ADMIN);
            memberRepository.save(newAdmin);

            // Gửi thông báo cho tất cả thành viên trong nhóm
            ConversationDTO updatedConversation = mapToDTO(conversationRepository.findById(conversationId).get());
            for (ObjectId memberId : updatedConversation.getMemberId()) {
                simpMessagingTemplate.convertAndSend(
                        "/chat/update/group/" + memberId,
                        updatedConversation
                );
            }
        }

        // Xóa người dùng khỏi danh sách thành viên của cuộc trò chuyện
        conversationDTO.getMemberId().remove(user.getId());

        System.out.println("conversationDTO.getMemberId(): " + conversationDTO.getMemberId());

        this.saveConversation(conversationDTO, user.getId(), conversationDTO.getMemberId(), Optional.empty());

        conversationRepository.save(mapToEntity(conversationDTO));

        // Xóa thành viên khỏi cơ sở dữ liệu
        memberRepository.deleteByConversationIdAndUserId(conversationId, user.getId());

        // Luu message khi người dùng rời khỏi nhóm
        Message message = Message.builder()
                .conversationId(conversationDTO.getId())
                .messageType(MessageType.SYSTEM)
                .content(user.getDisplayName() + " đã rời khỏi nhóm")
                .timestamp(Instant.now())
                .recalled(false)
                .build();

        message = messageRepository.save(message);
        return message;

    }

    public Message removeGroup(ObjectId conversationId, String token, ObjectId userId) {

        // Lấy thông tin người dùng từ token
        UserResponse user = userService.getCurrentUser(token);

        User userDelete = userRepository.findById(userId)
                .orElseThrow(() -> new ConversationCreationException("Người dùng không tồn tại"));

        if(user == null) {
            throw new ConversationCreationException("Người dùng không tồn tại");
        }

        // Tìm cuộc trò chuyện theo ID
        ConversationDTO conversationDTO = this.findConversationById(conversationId);
        if(conversationDTO == null) {
            throw new ConversationCreationException("Cuộc trò chuyện không tồn tại");
        }

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if(!conversationDTO.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        // Kiểm tra xem người dùng có phải là thành viên của cuộc trò chuyện không
        if(!this.isMember(conversationId, userDelete.getId())) {
            throw new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện");
        }

        //Kiểm tra xem người dùng có phải là admin không
        Member memberCurrent  = memberRepository.findByConversationIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        Member memberDelete  = memberRepository.findByConversationIdAndUserId(conversationId, userDelete.getId())
                .orElseThrow(() -> new ConversationCreationException("Người dùng cần xóa không phải là thành viên của cuộc trò chuyện"));

        System.out.println("memberDelete: " + memberDelete);
        System.out.println("member: " + memberCurrent);

        if(memberCurrent.getRole() != MemberRoles.ADMIN) {
            throw new UnauthorizedException("Chỉ admin mới có quyền xóa thành viên");
        }

        if(memberDelete.getId().equals(memberCurrent.getId())) {
            throw new ConversationCreationException("Không thể xóa chính mình");
        }

        // Xóa người dùng khỏi danh sách thành viên của cuộc trò chuyện
        conversationDTO.getMemberId().remove(userDelete.getId());

        System.out.println("conversationDTO.getMemberId(): " + conversationDTO.getMemberId());

        this.saveConversation(conversationDTO, userDelete.getId(), conversationDTO.getMemberId(), Optional.empty());

        conversationRepository.save(mapToEntity(conversationDTO));

        // Xóa thành viên khỏi cơ sở dữ liệu
        memberRepository.deleteByConversationIdAndUserId(conversationId, userDelete.getId());

        Message message = Message.builder()
                .conversationId(conversationDTO.getId())
                .messageType(MessageType.SYSTEM)
                .content(userDelete.getDisplayName() + " đã được " +user.getDisplayName()+ " xóa ra khỏi nhóm")
                .timestamp(Instant.now())
                .recalled(false)
                .build();

        message = messageRepository.save(message);
        return message;
    }

    public Message addMemberGroup(ObjectId conversationId, ObjectId userId) {

        // kiểm tra thông tin uesr có tồn tại chưa
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConversationCreationException("Người dùng không tồn tại"));
        // Tìm cuộc trò chuyện theo ID
        ConversationDTO conversationDTO = this.findConversationById(conversationId);
        if(conversationDTO == null) {
            throw new ConversationCreationException("Cuộc trò chuyện không tồn tại");
        }

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if(!conversationDTO.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }


        // Kiểm tra xem người dùng đã có trong danh sách thành viên chưa
        if(conversationDTO.getMemberId().contains(user.getId())) {
            throw new ConversationCreationException("Người dùng đã có trong danh sách thành viên");
        }
        // Thêm người dùng vào danh sách thành viên của cuộc trò chuyện
        conversationDTO.getMemberId().add(user.getId());
        System.out.println("conversationDTO.getMemberId(): " + conversationDTO.getMemberId());
        this.saveConversation(conversationDTO, user.getId(), conversationDTO.getMemberId(), Optional.empty());
        conversationRepository.save(mapToEntity(conversationDTO));
        // Lưu thành viên vào cơ sở dữ liệu
        Member newMember = Member.builder()
                .conversationId(conversationId)
                .userId(user.getId())
                .role(MemberRoles.MEMBER)
                .joinedAt(Instant.now())
                .build();
        memberRepository.save(newMember);

        // lấy tên người dùng theo id
        User userAdd = userRepository.findById(user.getId())
                .orElseThrow(() -> new ConversationCreationException("Người dùng không tồn tại"));
        // Luu message khi người dùng tham gia nhóm
        Message message = Message.builder()
                .conversationId(conversationDTO.getId())
                .messageType(MessageType.SYSTEM)
                .content(userAdd.getDisplayName() + " đã tham gia nhóm")
                .timestamp(Instant.now())
                .recalled(false)
                .build();
        message = messageRepository.save(message);
        return message;
    }

    // findUserByIDConversation
    @Override
    public List<MemberResponse> findUserByIDConversation(ObjectId conversationId) {
        // Tìm cuộc trò chuyện theo ID
        ConversationDTO conversationDTO = this.findConversationById(conversationId);
        if (conversationDTO == null) {
            throw new ConversationCreationException("Cuộc trò chuyện không tồn tại");
        }

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if (!conversationDTO.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        // Lấy danh sách thành viên của cuộc trò chuyện
        List<Member> members = memberRepository.findByConversationId(conversationId);

        // Chuyển đổi danh sách Member thành MemberResponse
        List<MemberResponse> memberResponses = members.stream()
                .map(member -> {
                    // Tìm thông tin người dùng từ UserRepository
                    User user = userRepository.findById(member.getUserId())
                            .orElse(null);
                    if (user == null) {
                        return null; // Bỏ qua nếu không tìm thấy user
                    }
                    return MemberResponse.builder()
                            .id(user.getId())
                            .displayName(user.getDisplayName())
                            .avatar(user.getAvatar())
                            .phone(user.getPhone())
                            .role(member.getRole()) // Lấy vai trò từ Member
                            .build();
                })
                .filter(Objects::nonNull) // Loại bỏ các phần tử null
                .collect(Collectors.toList());

        return memberResponses;
    }

    @Override
    public ConversationDTO updateMemberRole(ObjectId conversationId, ObjectId memberId, String newRole, ObjectId requestingUserId) {
        // Kiểm tra xem cuộc trò chuyện có tồn tại không
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện có ID: " + conversationId));

        // nếu cuộc trò chuyện không phải là nhóm thì không thể cập nhật vai trò
        if (!conversation.isGroup()) {
            throw new ConversationCreationException("Không thể cập nhật vai trò trong cuộc trò chuyện một-một");
        }

        // Kiểm tra xem người dùng yêu cầu có phải là admin không
        MemberRoles roleEnum;
        try {
            roleEnum = MemberRoles.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConversationCreationException("Invalid role: " + newRole + ". Valid roles are: " + Arrays.toString(MemberRoles.values()));
        }

        // Tìm thành viên yêu cầu
        Member requestingMember = memberRepository.findByConversationIdAndUserId(conversationId, requestingUserId)
                .orElseThrow(() -> new ConversationCreationException("Người dùng yêu cầu không phải là thành viên của cuộc trò chuyện này"));

        // Kiểm tra xem người yêu cầu có phải là admin không
        if (requestingMember.getRole() != MemberRoles.ADMIN) {
            throw new ConversationCreationException("Chỉ có quản trị viên nhóm mới có thể cập nhật vai trò của thành viên");
        }

        // Tìm thành viên cần cập nhật vai trò
        Member memberToUpdate = memberRepository.findByConversationIdAndUserId(conversationId, memberId)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy thành viên trong cuộc trò chuyện này"));

        // Kiểm tra xem người yêu cầu có phải là admin không
        if (memberToUpdate.getUserId().equals(requestingUserId)) {
            throw new ConversationCreationException("Admin không thể tự cập nhật vai trò của mình");
        }

        // Nếu vai trò mới là ADMIN, kiểm tra xem có thành viên nào khác là ADMIN không
        if (roleEnum == MemberRoles.ADMIN) {
            List<Member> allMembers = memberRepository.findByConversationId(conversationId);
            for (Member member : allMembers) {
                if (member.getRole() == MemberRoles.ADMIN && !member.getUserId().equals(memberId)) {
                    member.setRole(MemberRoles.MEMBER);
                    memberRepository.save(member);
                }
            }
        }

        // Update the member's role
        memberToUpdate.setRole(roleEnum);
        memberRepository.save(memberToUpdate);

        // Gửi thông báo cho tất cả thành viên trong nhóm
        return mapToDTO(conversation);
    }

    @Override
    public ConversationDTO deleteConversationForUser(ObjectId conversationId, ObjectId userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));


        if (!conversation.getMemberId().contains(userId)) {
            throw new UnauthorizedException("Bạn không phải thành viên của cuộc trò chuyện này");
        }

        if (conversation.getRemovedByUserIds() == null) {
            conversation.setRemovedByUserIds(new HashSet<>());
        }

        // Đánh dấu cuộc trò chuyện là đã bị xóa bởi người dùng
        conversation.getRemovedByUserIds().add(userId);

        // Xóa thành viên khỏi cơ sở dữ liệu
         memberRepository.deleteByConversationIdAndUserId(conversationId, userId);

        // Cập nhật lại danh sách thành viên
        conversation.getMemberId().remove(userId);

        // Nêú cuộc trò chuyện không còn thành viên nào thì xóa cuộc trò chuyện
        if (conversation.getMemberId().isEmpty()) {
            // Xóa tất cả tin nhắn của cuộc trò chuyện
            messageRepository.deleteAllByConversationId(conversationId);

            // Xóa conversation
            conversationRepository.deleteById(conversationId);

            return null;
        }

        conversationRepository.save(conversation);

         return mapToDTO(conversation);
    }

    @Override
    public ConversationDTO updateGroupName(ObjectId conversationId, String newGroupName) {

        // Kiểm tra xem cuộc trò chuyện có tồn tại không
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if (!conversation.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        // Cập nhật tên nhóm
        conversation.setName(newGroupName);
        conversationRepository.save(conversation);

        return mapToDTO(conversation);


    }

    // tìm linkgroup theo conversationId

    @Override
    public String findLinkGroupByConversationId(ObjectId conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if (!conversation.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        return conversation.getLinkGroup();
    }


    // tìm conversationId theo linkGroup

    @Override
    public ConversationDTO findConversationIdByLinkGroup(String linkGroup) {
        Conversation conversation = conversationRepository.findByLinkGroup(linkGroup)
                .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện với linkGroup: " + linkGroup));

        // Kiểm tra xem cuộc trò chuyện có phải là nhóm không
        if (!conversation.isGroup()) {
            throw new ConversationCreationException("Cuộc trò chuyện không phải là nhóm");
        }

        return mapToDTO(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> findAllGroupConversationsByUserId(ObjectId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId không được null");
        }

        // Find all members where userId is a member
        List<Member> members = memberRepository.findByUserId(userId);
        if (members.isEmpty()) {
            log.debug("No group conversations found for userId: {}", userId);
            return Collections.emptyList();
        }

        // Get conversation IDs where the user is a member
        List<ObjectId> conversationIds = members.stream()
                .map(Member::getConversationId)
                .collect(Collectors.toList());

        // Find all group conversations
        List<Conversation> groupConversations = conversationRepository.findByIdInAndIsGroupTrue(conversationIds);
        if (groupConversations.isEmpty()) {
            log.debug("No group conversations found for userId: {}", userId);
            return Collections.emptyList();
        }

        // Map to ConversationDTO
        return groupConversations.stream().map(conversation -> {
            ConversationDTO dto = mapToDTO(conversation);

            // Populate members and roles
            List<Member> allMembers = memberRepository.findByConversationId(conversation.getId());
            Map<ObjectId, MemberRoles> roleMap = allMembers.stream()
                    .collect(Collectors.toMap(Member::getUserId, Member::getRole));
            Set<ObjectId> memberIds = roleMap.keySet();
            dto.setMemberId(memberIds);

            // Fetch user details
            List<UserResponse> userResponses = userService.getUsersByIds(memberIds);
            List<MemberResponse> memberResponses = userResponses.stream()
                    .map(user -> toMemberResponse(user, roleMap.get(user.getId())))
                    .collect(Collectors.toList());
            dto.setMembers(memberResponses);

            // Populate lastMessage if available
            if (conversation.getLastMessageId() != null) {
                Optional<Message> lastMessageOpt = messageRepository.findById(conversation.getLastMessageId());
                lastMessageOpt.ifPresent(message -> {
                    MessageDTO messageDTO = modelMapper.map(message, MessageDTO.class);
                    dto.setLastMessage(messageDTO);
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }


}
