package vn.edu.iuh.fit.services;

import java.util.List;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;

public interface MessageService {
    MessageDTO sendMessage(ChatMessageRequest request);
    List<Message> getMessages(String conversationId);

    public Message recallMessage(ObjectId messageId, ObjectId senderId, ObjectId conversationId);
    Message deleteMessageForUser(ObjectId messageId, ObjectId userId);

    Message getMessageById(ObjectId messageId);

    Message pinMessage(ObjectId messageId, ObjectId userId, ObjectId conversationId);
    Message unpinMessage(ObjectId messageId, ObjectId userId, ObjectId conversationId);

    List<MessageDTO> getPinnedMessages(ObjectId conversationId);

    // Add voting method
    Message voteInPoll(ObjectId messageId, ObjectId userId, int optionIndex);

    List<MessageDTO> searchMessages(ObjectId conversationId, String keyword);
}