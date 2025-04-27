package vn.edu.iuh.fit.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRequestDTO {
    private String callerId;
    private String recipientId;
    private String conversationId;
    private String callerName;
    private String callType;
    // Getters và setters
}