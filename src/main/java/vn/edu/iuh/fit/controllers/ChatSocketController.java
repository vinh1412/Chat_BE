package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.dtos.AnswerDTO;
import vn.edu.iuh.fit.dtos.IceCandidateDTO;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.OfferDTO;
import vn.edu.iuh.fit.dtos.request.CallRequestDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.dtos.response.CallResponseDTO;
import vn.edu.iuh.fit.services.MessageService;

@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat.send/{conversationId}")
    public void send(@Payload ChatMessageRequest messageRequest, @DestinationVariable String conversationId) {
        MessageDTO savedMessage = messageService.sendMessage(messageRequest);

        // Gửi về người nhận
        messagingTemplate.convertAndSendToUser(
                messageRequest.getReceiverId(),
                "/queue/messages",
                savedMessage
        );

        // Gửi về người gửi luôn (để hiển thị chính họ)
        messagingTemplate.convertAndSendToUser(
                messageRequest.getSenderId(),
                "/queue/messages",
                savedMessage
        );
    }


    @MessageMapping("/call/request")
    public void handleCallRequest(CallRequestDTO request) {
        CallResponseDTO response = new CallResponseDTO();
        response.setStatus("request");
        response.setCallerId(request.getCallerId());
        response.setRecipientId(request.getRecipientId());
        response.setConversationId(request.getConversationId());
        response.setCallerName(request.getCallerName());
        response.setCallType(request.getCallType());
        System.out.println("Sending call request to recipient: " + request.getRecipientId());
        messagingTemplate.convertAndSend(
                "/user/" + request.getRecipientId() + "/call",
                response
        );
    }

    @MessageMapping("/call/accept")
    public void handleCallAccept(CallResponseDTO response) {
        messagingTemplate.convertAndSend(
                "/user/" + response.getCallerId() + "/call",
                response
        );
    }

    @MessageMapping("/call/reject")
    public void handleCallReject(CallResponseDTO response) {
        messagingTemplate.convertAndSend(
                "/user/" + response.getCallerId() + "/call",
                response
        );
    }

    @MessageMapping("/call/end")
    public void handleCallEnd(CallResponseDTO response) {
        response.setStatus("ended");
        messagingTemplate.convertAndSend(
                "/user/" + response.getCallerId() + "/call",
                response
        );
        messagingTemplate.convertAndSend(
                "/user/" + response.getRecipientId() + "/call",
                response
        );
    }

    @MessageMapping("/call/offer")
    public void handleOffer(OfferDTO data) {
        CallResponseDTO response = new CallResponseDTO();
        response.setStatus("offer");
        response.setOffer(data.getOffer());
        response.setCallerId(data.getCallerId());
        response.setRecipientId(data.getRecipientId());
        messagingTemplate.convertAndSend(
                "/user/" + data.getRecipientId() + "/call",
                response
        );
    }

    @MessageMapping("/call/answer")
    public void handleAnswer(AnswerDTO data) {
        CallResponseDTO response = new CallResponseDTO();
        response.setStatus("answer");
        response.setAnswer(data.getAnswer());
        response.setCallerId(data.getCallerId());
        response.setRecipientId(data.getRecipientId());
        messagingTemplate.convertAndSend(
                "/user/" + data.getCallerId() + "/call",
                response
        );
    }

    @MessageMapping("/call/ice-candidate")
    public void handleIceCandidate(IceCandidateDTO data) {
        CallResponseDTO response = new CallResponseDTO();
        response.setStatus("ice-candidate");
        response.setCandidate(data.getCandidate());
        response.setCallerId(data.getCallerId());
        response.setRecipientId(data.getRecipientId());
        messagingTemplate.convertAndSend(
                "/user/" + data.getRecipientId() + "/call",
                response
        );
    }

}
