package vn.edu.iuh.fit.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.ConversationDTO;

import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.dtos.request.FileRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.entities.Conversation;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.entities.PollOption;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.repositories.ConversationRepository;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.ImageService;
import vn.edu.iuh.fit.services.MessageService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private CloudinaryService cloudinaryService;


    @Autowired
    private final MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ImageService imageService;

    @Autowired
    private final ConversationService conversationService;

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private final MemberRepository memberRepository;

    @PostMapping
    @MessageMapping("/chat/send")
    public ResponseEntity<ApiResponse<?>> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            System.out.println("Request: " + request);

            // Kiểm tra nếu là nhóm và có giới hạn nhắn tin
            Conversation conversation = conversationRepository.findById(new ObjectId(request.getConversationId()))
                    .orElseThrow(() -> new ConversationCreationException("Không tìm thấy cuộc trò chuyện"));
            if (conversation.isGroup() && conversation.isRestrictMessagingToAdmin()) {
                Member member = memberRepository.findByConversationIdAndUserId(
                        new ObjectId(request.getConversationId()),
                        new ObjectId(request.getSenderId())
                ).orElseThrow(() -> new ConversationCreationException("Người dùng không phải là thành viên của cuộc trò chuyện"));
                if (member.getRole() != MemberRoles.ADMIN) {
                    return ResponseEntity.badRequest().body(ApiResponse.builder()
                            .status("FAILED")
                            .message("Chỉ admin mới có thể nhắn tin trong nhóm này")
                            .build());
                }
            }

            MessageDTO message = messageService.sendMessage(request);
            messagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Send message successfully")
                    .response(message)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/recall")
    @MessageMapping("/chat/recall")
    public ResponseEntity<ApiResponse<?>> recallMessage(@RequestBody Map<String, String> request) {
        try {
            System.out.println("Request: " + request);
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId senderId = new ObjectId(request.get("senderId"));
            ObjectId conversationId = new ObjectId(request.get("conversationId"));

            Message messageRecall = messageService.recallMessage(messageId, senderId, conversationId);
            if (messageRecall == null) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found or not belong to this user")
                        .build());
            }
            System.out.println("Message recalled: " + messageRecall);
            // Send the recalled message to the client
            messagingTemplate.convertAndSend("/chat/message/single/" + messageRecall.getConversationId(), messageRecall);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Recall message successfully")
                    .response(messageRecall)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getMessages(@PathVariable String conversationId) {
        try {
            System.out.println("Conversation ID: " + conversationId);
            List<Message> messages = messageService.getMessages(conversationId);
            messagingTemplate.convertAndSend("/chat/messages/" + conversationId, messages);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Fetch messages successfully")
                    .response(messages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }


    @PostMapping("/delete-for-user")
    @MessageMapping("/chat/delete-for-user")
    public ResponseEntity<ApiResponse<?>> deleteMessageForUser(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId userId = new ObjectId(request.get("userId"));

            System.out.println("messageId " + messageId + "- userId " + userId);

            Message updatedMessage = messageService.deleteMessageForUser(messageId, userId);

            if (updatedMessage == null) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found")
                        .build());
            }

            messagingTemplate.convertAndSend("/chat/message/single/" + updatedMessage.getConversationId(), updatedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message deleted for user")
                    .response(updatedMessage)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping(value = "/upload-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadImage(
            @RequestPart("request") String reqJson,
            @RequestPart(value = "anh", required = false) List<MultipartFile> anh) {
        ObjectMapper objectMapper = new ObjectMapper();
        ChatMessageRequest chatMessageRequest;

        try {
            chatMessageRequest = objectMapper.readValue(reqJson, ChatMessageRequest.class);

            List<String> fileUrls = new ArrayList<>();
            List<FileRequest> fileRequests = new ArrayList<>();

            if (anh != null && !anh.isEmpty()) {
                for (MultipartFile file : anh) {
                    String fileUrl = null;
                    if (chatMessageRequest.getMessageType().equals("VIDEO")) {
                        fileUrl = cloudinaryService.uploadVideo(file);
                    } else if (chatMessageRequest.getMessageType().equals("IMAGE")) {
                        fileUrl = cloudinaryService.uploadImage(file);
                    } else {
                        fileUrl = cloudinaryService.uploadFile(file);
                    }
                    fileUrls.add(fileUrl);
                    FileRequest fileRequest = FileRequest.builder()
                            .fileName(file.getOriginalFilename())
                            .fileType(file.getContentType())
                            .fileUrl(fileUrl)
                            .uploadedAt(Instant.now())
                            .build();
                    imageService.saveImage(fileRequest);
                }
            }
            // Trả về danh sách URL file và chatMessageRequest cho client
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Upload thành công")
                    .response(Map.of("fileUrls", fileUrls, "chatMessageRequest", chatMessageRequest))
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message("Định dạng yêu cầu không hợp lệ: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/check-members")
    public ResponseEntity<ApiResponse<?>> checkMembers(@RequestBody Map<String, String> request) {
        try {
            ObjectId senderId = new ObjectId(request.get("senderId"));
            ObjectId receiverId = new ObjectId(request.get("receiverId"));
            ConversationDTO conversation = conversationService.findConversationByMembers(senderId, receiverId);
            if (conversation == null) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Conversation not found")
                        .build());
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Check members successfully")
                    .response(conversation)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/forward")
    @MessageMapping("/chat/forward")
    public ResponseEntity<ApiResponse<?>> forwardMessage(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId senderId = new ObjectId(request.get("senderId"));
            String receiverId = request.get("receiverId"); // Có thể là userId hoặc groupId
            String content = request.get("content"); // Nội dung tin nhắn gốc

            System.out.println("senderId: " + senderId + " receiverId: " + receiverId + " content: " + content);

            // Tìm tin nhắn gốc
//            Message originalMessage = messageService.getMessageById(messageId);
//            if (originalMessage == null || !originalMessage.getSenderId().equals(senderId)) {
//                return ResponseEntity.badRequest().body(ApiResponse.builder()
//                        .status("FAILED")
//                        .message("Message not found or unauthorized")
//                        .build());
//            }

            // Tìm hoặc tạo cuộc trò chuyện đích
            ConversationDTO conversation = conversationService.findOrCreateConversation(senderId, receiverId);

            // Tạo tin nhắn mới
            ChatMessageRequest forwardRequest = new ChatMessageRequest();
            forwardRequest.setConversationId(conversation.getId().toString());
            forwardRequest.setSenderId(senderId.toString());
            forwardRequest.setReceiverId(receiverId);
            forwardRequest.setContent(content);
            forwardRequest.setMessageType("TEXT"); // Có thể mở rộng cho hình ảnh/file

            MessageDTO forwardedMessage = messageService.sendMessage(forwardRequest);

            // Gửi qua WebSocket
            messagingTemplate.convertAndSend("/chat/message/single/" + forwardedMessage.getConversationId(), forwardedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message forwarded successfully")
                    .response(forwardedMessage)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/pin")
    @MessageMapping("/chat/pin")
    public ResponseEntity<ApiResponse<?>> pinMessage(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId userId = new ObjectId(request.get("userId"));
            ObjectId conversationId = new ObjectId(request.get("conversationId"));

            Message pinnedMessage = messageService.pinMessage(messageId, userId, conversationId);

            // khi pin thành công, gửi thông báo đến tất cả người dùng trong cuộc trò chuyện
            messagingTemplate.convertAndSend("/chat/message/single/" + conversationId, pinnedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message pinned successfully")
                    .response(pinnedMessage)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/unpin")
    @MessageMapping("/chat/unpin")
    public ResponseEntity<ApiResponse<?>> unpinMessage(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId userId = new ObjectId(request.get("userId"));
            ObjectId conversationId = new ObjectId(request.get("conversationId"));

            Message unpinnedMessage = messageService.unpinMessage(messageId, userId, conversationId);

            // khi unpin thành công, gửi thông báo đến tất cả người dùng trong cuộc trò chuyện
            messagingTemplate.convertAndSend("/chat/message/single/" + conversationId, unpinnedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message unpinned successfully")
                    .response(unpinnedMessage)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/pinned/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getPinnedMessages(@PathVariable String conversationId) {
        try {
            if (!ObjectId.isValid(conversationId)) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Invalid conversation ID format")
                        .build());
            }
            List<MessageDTO> pinnedMessages = messageService.getPinnedMessages(new ObjectId(conversationId));
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Fetch pinned messages successfully")
                    .response(pinnedMessages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/vote")
    @MessageMapping("/chat/vote")
    public ResponseEntity<ApiResponse<?>> voteOnPoll(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId userId = new ObjectId(request.get("userId"));
            int optionIndex = Integer.parseInt(request.get("optionIndex"));

            Message message = messageService.getMessageById(messageId);
            if (message == null || message.getMessageType() != MessageType.POLL) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found or not a poll")
                        .build());
            }

            List<PollOption> pollOptions = message.getPollOptions();
            if (optionIndex < 0 || optionIndex >= pollOptions.size()) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Invalid option index")
                        .build());
            }

            // Remove user's vote from other options
            pollOptions.forEach(option -> option.getVoters().remove(userId));
            // Add vote to the selected option
            pollOptions.get(optionIndex).getVoters().add(userId);

            Message updatedMessage = messageRepository.save(message);
            messagingTemplate.convertAndSend("/chat/message/single/" + updatedMessage.getConversationId(), updatedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Voted successfully")
                    .response(updatedMessage)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/{messageId}/vote")
    public ResponseEntity<?> voteInPoll(
            @PathVariable String messageId,
            @RequestParam int optionIndex,
            @RequestHeader("Authorization") String token,
            @RequestParam String userId) {
        try {
            Message updatedMessage = messageService.voteInPoll(new ObjectId(messageId), new ObjectId(userId), optionIndex);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Vote recorded successfully",
                    "updatedMessageId", updatedMessage.getId().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchMessages(
            @RequestParam String conversationId,
            @RequestParam(required = false) String keyword) {
        try {
            ObjectId conversationObjId = new ObjectId(conversationId);
            List<MessageDTO> messages = messageService.searchMessages(conversationObjId, keyword);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Search messages successfully")
                    .response(messages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }
}