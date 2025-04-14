package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;

import java.util.List;

public interface MessageService {
    Message sendMessage(ChatMessageRequest request);
    List<Message> getMessages(String conversationId);
}

