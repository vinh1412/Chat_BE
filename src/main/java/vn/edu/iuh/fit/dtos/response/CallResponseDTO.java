package vn.edu.iuh.fit.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallResponseDTO {
    private String status;
    private String callerId;
    private String recipientId;
    private String conversationId;
    private String callerName;
    private String callType;
    private Object offer;
    private Object answer;
    private Object candidate;

    // Getters và setters
}
