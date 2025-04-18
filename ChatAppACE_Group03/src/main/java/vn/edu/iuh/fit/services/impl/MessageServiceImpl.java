package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.services.MessageService;

import java.net.URL;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    // Phương thức kiểm tra ObjectId hợp lệ
    private boolean isValidObjectId(String str) {
        return str != null && str.matches("[a-fA-F0-9]{24}"); // Kiểm tra độ dài 24 ký tự và chỉ chứa các ký tự hex
    }

    private MessageDTO convertToDTO(Message message) {
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

        // Kiểm tra URL hợp lệ cho GIF/STICKER
        if (messageType == MessageType.GIF || messageType == MessageType.STICKER) {
            if (request.getFileUrl() == null || request.getFileUrl().isEmpty()) {
                throw new IllegalArgumentException("File URL cannot be empty for GIF/STICKER messages");
            }
            // Pass both the URL and the MessageType to the isValidUrl method
            if (!isValidUrl(request.getFileUrl(), messageType)) {
                throw new IllegalArgumentException("Invalid file URL for GIF/STICKER");
            }
        }
        Message message = Message.builder()
                .senderId(new ObjectId(request.getSenderId()))
                .conversationId(new ObjectId(request.getConversationId()))
                .messageType(messageType)
                .timestamp(Instant.now())
                .isSeen(false)
                .recalled(false)
                .build();

        // Xử lý nội dung dựa trên loại tin nhắn
        switch (messageType) {
            case TEXT:
                message.setContent(request.getContent());
                break;
            case GIF:
                message.setFileUrl(request.getFileUrl());
                break;
            case EMOJI:
                message.setContent(request.getContent());
                message.setFileUrl(request.getFileUrl());
                break;
            case STICKER:
                message.setFileUrl(request.getFileUrl());
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

}

