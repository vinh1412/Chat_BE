package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.entities.PollOption;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.services.MessageService;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationServiceImpl conversationService;
    private final UserServiceImpl userService;

    // Phương thức kiểm tra ObjectId hợp lệ
    private boolean isValidObjectId(String str) {
        return str != null && str.matches("[a-fA-F0-9]{24}"); // Kiểm tra độ dài 24 ký tự và chỉ chứa các ký tự hex
    }

    private MessageDTO convertToDTO(Message message) {
        String senderName = userService.getUserByIds(message.getSenderId())
                .map(user -> user.getDisplayName())
                .orElse("Unknown User"); // Lấy tên người gửi từ dịch vụ người dùng
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileUrl(message.getFileUrl())
                .timestamp(message.getTimestamp())
                .isSeen(message.isSeen())
                .recalled(message.isRecalled())
                .replyToMessageId(message.getReplyToMessageId())
                .reactions(message.getReactions())
                .deletedBy(message.getDeletedBy())
                .senderName(senderName) // Gán tên người gửi
                .build();
    }


//    @Override
//    public Message sendMessage(ChatMessageRequest request) {
//        // Kiểm tra tính hợp lệ của senderId và conversationId
//        if (!isValidObjectId(request.getSenderId()) || !isValidObjectId(request.getConversationId())) {
//            throw new IllegalArgumentException("Invalid ObjectId format");
//        }
//
//        Message message = Message.builder()
//                .senderId(new ObjectId(request.getSenderId()))
//                .conversationId(new ObjectId(request.getConversationId()))
//                .content(request.getContent())
//                .messageType(MessageType.valueOf(request.getMessageType()))
//                .fileUrl(request.getFileUrl())
//                .timestamp(Instant.now())
//                .isSeen(false)
//                .recalled(false)
//                .replyToMessageId(request.getReplyToMessageId() != null && isValidObjectId(request.getReplyToMessageId()) ? new ObjectId(request.getReplyToMessageId()) : null)
//                .build();
//
//        return messageRepository.save(message);
//    }

    @Override
    public MessageDTO sendMessage(ChatMessageRequest request) {
        if (!isValidObjectId(request.getSenderId()) || !isValidObjectId(request.getConversationId())) {
            throw new IllegalArgumentException("Invalid ObjectId format");
        }

        MessageType messageType = MessageType.valueOf(request.getMessageType());
        System.out.println("MessageType: " + messageType);

        // Kiểm tra URL hợp lệ cho GIF/STICKER
//        if (messageType == MessageType.GIF || messageType == MessageType.STICKER || messageType == MessageType.EMOJI || messageType == MessageType.IMAGE || messageType == MessageType.FILE || messageType == MessageType.VIDEO) {
//            if (request.getFileUrl() == null || request.getFileUrl().isEmpty()) {
//                throw new IllegalArgumentException("File URL cannot be empty for GIF/STICKER messages");
//            }
//            // Pass both the URL and the MessageType to the isValidUrl method
//            if (!isValidUrl(request.getFileUrl(), messageType)) {
//                throw new IllegalArgumentException("Invalid file URL for GIF/STICKER");
//            }
//        }
        System.out.println("reques send: " + request);

        Message message = Message.builder()
                .senderId(new ObjectId(request.getSenderId()))
                .conversationId(new ObjectId(request.getConversationId()))
                .messageType(messageType)
                .timestamp(Instant.now())
                .isSeen(false)
                .recalled(false)
                .build();

        System.out.println("Message before save: " + message);

        // Xử lý nội dung dựa trên loại tin nhắn
        switch (messageType) {
            case TEXT:
                message.setContent(request.getContent());
                break;
            case GIF, IMAGE, STICKER, FILE, VIDEO:
                message.setFileUrl(request.getFileUrl());
                message.setContent(request.getContent());
                break;
            case EMOJI:
                message.setContent(request.getContent());
                message.setFileUrl(request.getFileUrl());
                break;
            case POLL:
                message.setContent(request.getContent()); // The poll question
                message.setPollOptions(request.getPollOptions() != null
                        ? request.getPollOptions().stream()
                        .map(opt -> PollOption.builder()
                                .optionText(opt)
                                .voters(new ArrayList<>())
                                .build())
                        .collect(Collectors.toList())
                        : null);
                if (message.getPollOptions() == null || message.getPollOptions().size() < 2) {
                    throw new IllegalArgumentException("Poll must have at least 2 options");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }

        // Nếu có trả lời tin nhắn trước đó
        if (request.getReplyToMessageId() != null && isValidObjectId(request.getReplyToMessageId())) {
            message.setReplyToMessageId(new ObjectId(request.getReplyToMessageId()));
        }

        // Lưu tin nhắn và chuyển đổi sang DTO
        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
        //Gửi tin nhắn qua WebSocket
    }

    // Hàm kiểm tra URL hợp lệ
    private boolean isValidUrl(String url, MessageType messageType) {
        try {
            new URL(url).toURI();
            if (messageType == MessageType.GIF) {
                return url.startsWith("https://media"); // Chỉ chấp nhận GIF từ GIPHY
            } else if (messageType == MessageType.STICKER) {
                return url.startsWith("https://your-cdn.com/"); // Chỉ chấp nhận sticker từ CDN của bạn
            } else if (messageType == MessageType.VIDEO) {
                return url.endsWith(".mp4") || url.endsWith(".avi") || url.endsWith(".mkv"); // Chỉ chấp nhận video với các định dạng này
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public List<Message> getMessages(String conversationId) {
        // Kiểm tra tính hợp lệ của conversationId trước khi gọi method
        if (!isValidObjectId(conversationId)) {
            throw new IllegalArgumentException("Invalid conversationId format");
        }
        return messageRepository.findByConversationIdOrderByTimestampAsc(new ObjectId(conversationId));
    }

    @Override
    public Message recallMessage(ObjectId messageId, ObjectId senderId, ObjectId conversationId) {
        if (messageId == null || senderId == null || conversationId == null) {
            throw new IllegalArgumentException("Message ID, Sender ID, and Conversation ID không thể null.");
        }
        // Tìm kiếm tin nhắn theo ID
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại."));
        // Kiểm tra xem người gửi có phải là người đã gửi tin nhắn không
        if (!message.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("Bạn không phai người gửi tin nhắn này.");
        }

        // kiểm tra message co thuọc về cuộc trò chuyện không
        if (!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Tin nhắn không thuộc về cuộc trò chuyện này.");
        }

        //Kieểm tra xem tin nhắn đã được thu hồi chưa và nếu chưa thì thu hồi
        if (!message.isRecalled()) {
            message.setRecalled(true);
            message.setContent("Tin nhắn đã được thu hồi.");

            if (message.getMessageType() == MessageType.GIF || message.getMessageType() == MessageType.STICKER ||
                    message.getMessageType() == MessageType.EMOJI || message.getMessageType() == MessageType.IMAGE ||
                    message.getMessageType() == MessageType.FILE || message.getMessageType() == MessageType.VIDEO) {
                message.setFileUrl(null); // Xóa URL nếu là GIF hoặc STICKER
                message.setMessageType(MessageType.TEXT); // Đặt lại loại tin nhắn thành TEXT
            }

            return messageRepository.save(message);
        }

        // Nếu tin nhắn đã được thu hồi thì không làm gì cả
        return message = Message.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileUrl(message.getFileUrl())
                .timestamp(message.getTimestamp())
                .isSeen(message.isSeen())
                .recalled(true)
                .replyToMessageId(message.getReplyToMessageId())
                .build();
    }

    @Override
    public Message deleteMessageForUser(ObjectId messageId, ObjectId userId) {
        // Tìm tin nhắn
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return null;
        }

        // Thêm userId vào danh sách người dùng đã xóa tin nhắn
        if (message.getDeletedByUserIds() == null) {
            message.setDeletedByUserIds(new HashSet<>());
        }

        //  Kiểm tra xem userId đã có trong danh sách chưa
        if (!message.getDeletedByUserIds().contains(userId)) {
            message.getDeletedByUserIds().add(userId);
            return messageRepository.save(message);
        }

        return message;
    }

    @Override
    public Message getMessageById(ObjectId messageId) {
        try {
            return messageRepository.findById(messageId).orElse(null);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid messageId format: " + messageId);
            return null;
        }
    }

    @Override
    public Message pinMessage(ObjectId messageId, ObjectId userId, ObjectId conversationId) {
        // Kiểm tra tính hợp lệ của messageId và conversationId
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Kiểm tra xem message có thuộc về conversation không
        if (!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Message does not belong to this conversation");
        }

        // Kiểm tra xem message đã được ghim chưa
        if (!conversationService.isMember(conversationId, userId)) {
            throw new IllegalArgumentException("User is not a member of this conversation");
        }

        // Pin the message
        message.setPinned(true);
        messageRepository.save(message);

        // Thêm messageId vào danh sách pinnedMessages của conversation
        conversationService.addPinnedMessage(conversationId, messageId);

        return message;
    }

    @Override
    public Message unpinMessage(ObjectId messageId, ObjectId userId, ObjectId conversationId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Message does not belong to this conversation");
        }

        if (!conversationService.isMember(conversationId, userId)) {
            throw new IllegalArgumentException("User is not a member of this conversation");
        }

        message.setPinned(false);
        messageRepository.save(message);

        conversationService.removePinnedMessage(conversationId, messageId);

        return message;
    }

    @Override
    public List<MessageDTO> getPinnedMessages(ObjectId conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID cannot be null.");
        }
        List<Message> pinnedMessages = messageRepository.findPinnedMessagesByConversationId(conversationId);
        return pinnedMessages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Message voteInPoll(ObjectId messageId, ObjectId userId, int optionIndex) {
        // Validate inputs
        if (messageId == null || userId == null) {
            throw new IllegalArgumentException("Message ID and User ID cannot be null");
        }

        // Retrieve the message
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Verify it's a poll message
        if (message.getMessageType() != MessageType.POLL) {
            throw new IllegalArgumentException("Message is not a poll");
        }

        // Validate option index
        List<PollOption> pollOptions = message.getPollOptions();
        if (pollOptions == null || optionIndex < 0 || optionIndex >= pollOptions.size()) {
            throw new IllegalArgumentException("Invalid poll option index");
        }

        // Get the selected option
        PollOption selectedOption = pollOptions.get(optionIndex);

        // Check if the user has already voted
        if (selectedOption.getVoters().contains(userId)) {
            throw new IllegalArgumentException("User has already voted for this option");
        }

        // Add user to voters list
        selectedOption.getVoters().add(userId);

        // Save the updated message
        return messageRepository.save(message);
    }

    @Override
    public List<MessageDTO> searchMessages(ObjectId conversationId, String keyword) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID cannot be null");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        System.out.println("Total messages found: " + messages.size());
        if (keyword != null) {
            System.out.println("Searching for keyword: " + keyword);
        }

        List<Message> filteredMessages = messages.stream()
                .filter(message ->
                        keyword == null || keyword.isEmpty() ||
                                (message.getContent() != null && message.getContent().toLowerCase().contains(keyword.toLowerCase()))
                )
                .collect(Collectors.toList());

        System.out.println("Filtered messages count: " + filteredMessages.size());

        return filteredMessages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

}

