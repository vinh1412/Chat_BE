package vn.edu.iuh.fit.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IceCandidateDTO {
    private String roomId;
    private Object candidate;
    private String recipientId;
    private String callerId;
    // Getters và setters
}