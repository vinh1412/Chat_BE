package vn.edu.iuh.fit.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class ChatMessageRequest {
    private String senderId;
    private String receiverId;
    private String conversationId;
    private String content;
    private String messageType;
    private String fileUrl;
    private String replyToMessageId;
    private boolean isSeen;
    private boolean recalled;
    private List<String> pollOptions;

}

