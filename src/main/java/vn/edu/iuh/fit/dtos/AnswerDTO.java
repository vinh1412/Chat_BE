package vn.edu.iuh.fit.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {
    private String roomId;
    private Object answer;
    private String recipientId;
    private String callerId;
    // Getters và setters
}
