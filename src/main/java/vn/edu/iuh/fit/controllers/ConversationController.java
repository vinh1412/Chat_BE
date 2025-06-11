/*
 * @ {#} ConversationController.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.MemberResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Conversation;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.repositories.ConversationRepository;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;
    private final UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable ObjectId id) {
        return ResponseEntity.ok(conversationService.findConversationById(id));
    }

    @PostMapping("/createConversationOneToOne")
    public ResponseEntity<ConversationDTO> createConversationOneToOne(@RequestBody ConversationDTO conversationDTO) {
        return ResponseEntity.ok(conversationService.createConversationOneToOne(conversationDTO));
    }

    @GetMapping("/getAllConversationsByUserId")
    public ResponseEntity<?> getAllConversationsByUserId(@RequestHeader("Authorization") String token) {
        UserResponse user = userService.getCurrentUser(token);
        return ResponseEntity.ok(conversationService.findAllConversationsByUserId(user.getId()));
    }

//    @PostMapping("/createConversationGroup")
//    public ResponseEntity<ConversationDTO> createConversationGroup(@RequestHeader("Authorization") String token, @RequestBody ConversationDTO conversationDTO) {
//        try {
//            UserResponse user = userService.getCurrentUser(token);
//
//            ConversationDTO conversation = conversationService.createConversationGroup(user.getId(), conversationDTO);
//
//            for (ObjectId memberId : conversation.getMemberId()) {
//                System.out.println("memberId: " + memberId);
//                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, conversation);
//            }
//            return ResponseEntity.ok(conversation);
//        } catch (Exception e) {
//            System.out.println("Error creating group conversation: " + e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @PostMapping("/createConversationGroup")
    public ResponseEntity<ConversationDTO> createConversationGroup(
            @RequestHeader("Authorization") String token,
            @RequestBody ConversationDTO conversationDTO) {
        try {
            UserResponse user = userService.getCurrentUser(token);

            // Nếu chưa có linkGroup thì tự tạo
            if (conversationDTO.getLinkGroup() == null || conversationDTO.getLinkGroup().isEmpty()) {
                conversationDTO.setLinkGroup(generateRandomLinkGroup());
            }

            ConversationDTO conversation = conversationService.createConversationGroup(user.getId(), conversationDTO);

            for (ObjectId memberId : conversation.getMemberId()) {
                System.out.println("memberId: " + memberId);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, conversation);
            }
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            System.out.println("Error creating group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Gộp luôn hàm random bên dưới
    private String generateRandomLinkGroup() {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }

        return "iuhgroup3_" + randomString.toString();
    }

    @PostMapping("/find-or-create")
    public ResponseEntity<ConversationDTO> findOrCreateConversation(
            @RequestParam("senderId") String senderIdStr,
            @RequestParam("receiverId") String receiverId
    ) {
        try {
            ObjectId senderId = new ObjectId(senderIdStr);
            ConversationDTO conversation = conversationService.findOrCreateConversation(senderId, receiverId);
            System.out.println("Sender ID: " + senderId);
            System.out.println("Receiver ID: " + receiverId);
            System.out.println("Conversation: " + conversation);
            return ResponseEntity.ok(conversation);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ObjectId format: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/dissolve/{conversationId}")
    public ResponseEntity<?> dissolveConversation( @RequestHeader("Authorization") String token, @PathVariable ObjectId conversationId) {
        try {
            UserResponse user = userService.getCurrentUser(token);

            System.out.println("User ID: " + user.getId());
            System.out.println("Conversation ID: " + conversationId);
            Map<String, Object> result = conversationService.dissolveGroup(conversationId, user.getId()
            );

            if (!(boolean) result.get("success")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", result.get("message")));
            }

            // Lấy thông tin từ kết quả service
            List<Member> members = (List<Member>) result.get("members");
            ObjectId conversationIdRS = (ObjectId) result.get("conversationId");
            System.out.println("Conversation Id: " + conversationIdRS);

            ConversationDTO conversation = conversationService.findConversationById(conversationIdRS);

            System.out.println("Members to notify: " + members.size());
            // Gửi thông báo WebSocket cho tất cả thành viên
            for (Member member : members) {
                simpMessagingTemplate.convertAndSend(
                        "/chat/dissolve/group/" + member.getUserId(),
                        conversation
                );
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Nhóm đã được giải tán thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi giải tán nhóm: " + e.getMessage()));

        }
    }

    @DeleteMapping("/leave/{conversationId}")
    public ResponseEntity<?> leaveGroup(@PathVariable ObjectId conversationId, @RequestHeader("Authorization") String token) {
        System.out.println("Leave group conversation with ID: " + conversationId);
        try {
            Message message = conversationService.leaveGroup(conversationId, token);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);

            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId memberId : conversation.getMemberId()) {
                System.out.println("memberId: " + memberId);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, conversation);
            }

            return ResponseEntity.ok(message);
        } catch (ConversationCreationException e) {
            System.out.println("Error leaving group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/leave/{conversationId}/member/{memberId}")
    public ResponseEntity<?> removeMemberFromGroup(@PathVariable ObjectId conversationId, @PathVariable ObjectId memberId,@RequestHeader("Authorization") String token) {
        System.out.println("Leave group conversation with ID: " + conversationId);
        System.out.println("Member ID: " + memberId);
        try {
            Message message = conversationService.removeGroup(conversationId, token, memberId);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);

            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId member_id : conversation.getMemberId()) {
                System.out.println("memberId: " + member_id);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + member_id, conversation);
            }

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.out.println("Error leaving group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/add-member/{conversationId}")
    public ResponseEntity<?> addMemberToGroup(@PathVariable ObjectId conversationId, @RequestParam  ObjectId id) {
        System.out.println("Add member to group conversation with ID: " + conversationId);
        try {
            Message message = conversationService.addMemberGroup(conversationId, id);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);


            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId member_id : conversation.getMemberId()) {
                System.out.println("memberId: " + member_id);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + member_id, conversation);
            }

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.out.println("Error adding member to group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*
    test api
    http://localhost:8080/api/v1/conversations/add-member/68075bc43ec6ed45491a7c05
    {
        "idUser": "6807a181f727fc5e721618a7"
    }
     */

    @GetMapping("/members/{conversationId}")
    public ResponseEntity<List<MemberResponse>> getMembersByConversationId(@PathVariable ObjectId conversationId) {
        try {
            List<MemberResponse> members = conversationService.findUserByIDConversation(conversationId);
            // In danh sách thành viên ra console
            members.forEach(member ->
                    System.out.println("Display Name: " + member.getDisplayName() + ", Role: " + member.getRole())
            );
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            System.out.println("Error fetching members: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/update-role")
    public ResponseEntity<?> updateMemberRoleHttp(
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate the token and get the requesting user
            UserResponse requestingUser = userService.getCurrentUser(token);
            String requestingUserIdStr = requestingUser.getId().toString();
            System.out.println("Bắt đầu cập nhật vai trò, requestingUserId: " + requestingUserIdStr);

            // Extract data from the payload
            String conversationIdStr = payload.get("conversationId");
            String memberIdStr = payload.get("memberId");
            String newRole = payload.get("role");
            System.out.println("Payload: conversationId=" + conversationIdStr + ", memberId=" + memberIdStr + ", newRole=" + newRole);

            // Validate the payload
            if (conversationIdStr == null || memberIdStr == null || newRole == null || requestingUserIdStr == null) {
                System.out.println("Thiếu các trường bắt buộc trong payload");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu các trường bắt buộc trong payload"));
            }

            ObjectId conversationId = new ObjectId(conversationIdStr);
            ObjectId memberId = new ObjectId(memberIdStr);
            ObjectId requestingUserId = new ObjectId(requestingUserIdStr);

            // Update the member's role
            System.out.println("Gọi service để cập nhật vai trò...");
            ConversationDTO updatedConversation = conversationService.updateMemberRole(
                    conversationId,
                    memberId,
                    newRole,
                    requestingUserId
            );
            System.out.println("Cập nhật vai trò thành công, conversation: " + updatedConversation);

           User memberAdmin = userRepository.findById(requestingUserId).orElseThrow(() -> new RuntimeException("User not found"));
            User members = userRepository.findById(memberId).orElseThrow(() -> new RuntimeException("User not found"));

            Message message = Message.builder()
                    .conversationId(updatedConversation.getId())
                    .messageType(MessageType.SYSTEM)
                    .content(memberAdmin.getDisplayName() + " đã bổ nhệm "+members.getDisplayName()+" vai trò thành công")
                    .timestamp(Instant.now())
                    .recalled(false)
                    .build();

            messageRepository.save(message);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);


            // Notify all members via WebSocket
            for (ObjectId member : updatedConversation.getMemberId()) {
                System.out.println("Gửi thông báo WebSocket tới member: " + member);
                simpMessagingTemplate.convertAndSend(
                        "/chat/create/group/" + member,
                        updatedConversation
                );
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật vai trò thành công",
                    "conversation", updatedConversation
            ));

        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi cập nhật vai trò: " + e.getMessage()));
        }
    }

    @PostMapping("/delete-for-user/{conversationId}")
    public ResponseEntity<?> deleteConversationForUser(
            @PathVariable ObjectId conversationId,
            @RequestHeader("Authorization") String token) {
        try {
            UserResponse user = userService.getCurrentUser(token);
            ConversationDTO conversation = conversationService.deleteConversationForUser(conversationId, user.getId());
            if (conversation == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(Map.of("success", false, "message", "Cuộc trò chuyện xóa hoàn toàn"));
            }
            System.out.println("Cuộc trò chuyện đã được xóa cho người dùng: " + conversation);
            simpMessagingTemplate.convertAndSend("/chat/delete/" + user.getId(), conversation);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi xóa cuộc trò chuyện: " + e.getMessage()));
        }
    }
    // WebSocket endpoint for updating member role (for frontend)
//    @MessageMapping("/conversation/update-role")
//    public void updateMemberRoleWebSocket(Map<String, Object> payload) {
//        try {
//            // Extract data from the payload
//            String conversationIdStr = (String) payload.get("conversationId");
//            String memberIdStr = (String) payload.get("memberId");
//            String newRole = (String) payload.get("role");
//            String requestingUserIdStr = (String) payload.get("requestingUserId");
//
//            // Validate the payload
//            if (conversationIdStr == null || memberIdStr == null || newRole == null || requestingUserIdStr == null) {
//                throw new IllegalArgumentException("Thiếu các trường bắt buộc trong payload");
//            }
//
//            ObjectId conversationId = new ObjectId(conversationIdStr);
//            ObjectId memberId = new ObjectId(memberIdStr);
//            ObjectId requestingUserId = new ObjectId(requestingUserIdStr);
//
//            // Update the member's role
//            ConversationDTO updatedConversation = conversationService.updateMemberRole(
//                    conversationId,
//                    memberId,
//                    newRole,
//                    requestingUserId
//            );
//
//            // Notify all members via WebSocket
//            for (ObjectId member : updatedConversation.getMemberId()) {
//                simpMessagingTemplate.convertAndSend(
//                        "/chat/update/group/" + member,
//                        updatedConversation
//                );
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error updating member role: " + e.getMessage());
//            // Send an error message back to the client
//            String requestingUserIdStr = (String) payload.get("requestingUserId");
//            if (requestingUserIdStr != null) {
//                simpMessagingTemplate.convertAndSend(
//                        "/chat/error/" + requestingUserIdStr,
//                        Map.of("error", "Failed to update member role: " + e.getMessage())
//                );
//            }
//        }
//    }

//    UPDATE TÊN NHÓM
    @PutMapping("/update-group-name")
    public ResponseEntity<?> updateGroupName(@RequestBody Map<String, String> nameGroup,
                                             @RequestHeader("Authorization") String token) {
        try {
            UserResponse user = userService.getCurrentUser(token);
            String conversationIdStr = nameGroup.get("conversationId");
            String newGroupName = nameGroup.get("newGroupName");

            if (conversationIdStr == null || newGroupName == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu các trường bắt buộc trong payload"));
            }

            ObjectId conversationId = new ObjectId(conversationIdStr);

            ConversationDTO updatedConversation = conversationService.updateGroupName(conversationId, newGroupName);

            // Notify all members via WebSocket
            for (ObjectId member : updatedConversation.getMemberId()) {
                simpMessagingTemplate.convertAndSend(
                        "/chat/create/group/" + member,
                        updatedConversation
                );
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật tên nhóm thành công",
                    "conversation", updatedConversation
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi cập nhật tên nhóm: " + e.getMessage()));
        }

    }



    //findLinkGroupByConversationId
    @GetMapping("/linkGroup/{conversationId}")
    public ResponseEntity<?> findLinkGroupByConversationId(@PathVariable ObjectId conversationId) {
        try {
            String linkGroup = conversationService.findLinkGroupByConversationId(conversationId);
            return ResponseEntity.ok(Map.of("linkGroup", linkGroup));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi tìm kiếm link nhóm: " + e.getMessage()));
        }
    }
    // localhost:8080/api/v1/conversations/linkGroup/68075bc43ec6ed45491a7c05f

    @GetMapping("/conversationId/{linkGroup}")
    public ResponseEntity<?> findConversationIdByLinkGroup(@PathVariable String linkGroup) {
        try {
            ConversationDTO conversation = conversationService.findConversationIdByLinkGroup(linkGroup);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy cuộc trò chuyện với link nhóm này"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi tìm kiếm cuộc trò chuyện: " + e.getMessage()));
        }

    }

    @GetMapping("/getAllGroupConversationsByUserId")
    public ResponseEntity<?> getAllGroupConversationsByUserId(@RequestHeader("Authorization") String token) {
        try {
            UserResponse user = userService.getCurrentUser(token);
            List<ConversationDTO> groupConversations = conversationService.findAllGroupConversationsByUserId(user.getId());
            return ResponseEntity.ok(groupConversations);
        } catch (Exception e) {
            System.out.println("Error fetching group conversations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách nhóm: " + e.getMessage()));
        }
    }

    @PostMapping("/restrict-messaging")
    @MessageMapping("/conversation/restrict-messaging")
    public ResponseEntity<ApiResponse<?>> restrictMessaging(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        try {
            UserResponse user = userService.getCurrentUser(token);
            ObjectId conversationId = new ObjectId(request.get("conversationId"));
            boolean restrict = Boolean.parseBoolean(request.get("restrict"));

            // Kiểm tra xem người dùng có phải là admin không
            Member member = memberRepository.findByConversationIdAndUserId(conversationId, user.getId())
                    .orElseThrow(() -> new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện"));
            if (member.getRole() != MemberRoles.ADMIN) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Chỉ admin mới có thể thay đổi cài đặt nhắn tin")
                        .build());
            }

            // Cập nhật cài đặt giới hạn nhắn tin
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện"));
            if (!conversation.isGroup()) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Chỉ có thể áp dụng cho nhóm")
                        .build());
            }

            conversation.setRestrictMessagingToAdmin(restrict);
            conversationRepository.save(conversation);

            // Gửi thông báo hệ thống
            Message systemMessage = Message.builder()
                    .conversationId(conversationId)
                    .messageType(MessageType.SYSTEM)
                    .content(restrict ? "Chỉ admin được phép nhắn tin trong nhóm" : "Tất cả thành viên được phép nhắn tin trong nhóm")
                    .timestamp(Instant.now())
                    .recalled(false)
                    .build();
            messageRepository.save(systemMessage);

            // Gửi thông báo qua WebSocket
            ConversationDTO updatedConversation = conversationService.findConversationById(conversationId);
            for (ObjectId memberId : updatedConversation.getMemberId()) {
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, updatedConversation);
                simpMessagingTemplate.convertAndSend("/chat/message/single/" + conversationId, systemMessage);
            }

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Cập nhật cài đặt nhắn tin thành công")
                    .response(updatedConversation)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/add-member")
    public ResponseEntity<?> addMemberToGroupLink(
            @RequestParam ObjectId conversationId,
            @RequestParam ObjectId idUser
    ) {
        try {
            Message message = conversationService.addMemberGroup(conversationId, idUser);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);
            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId member_id : conversation.getMemberId()) {
                System.out.println("memberId: " + member_id);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + member_id, conversation);
            }
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.out.println("Error adding member to group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
