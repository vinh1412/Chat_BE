package vn.edu.iuh.fit.dtos.request;

import lombok.Data;

@Data
public class CreateConservationRequest {
    private String senderId;
    private String receiverId;
}
