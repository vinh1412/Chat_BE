/**
 * @ (#) SmsServiceImpl.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import vn.edu.iuh.fit.dtos.OtpEntry;
import vn.edu.iuh.fit.services.SmsService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
@Service
public class SmsServiceImpl implements SmsService {
    @Autowired
    private SnsClient snsClient;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final long OTP_TTL_MILLIS = 60_000;

    @Override
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    @Override
    public void sendOtp(String phoneNumber) {
        String otp = generateOtp();
        String formattedPhoneNumber = FormatPhoneNumber.formatPhoneNumberTo84(phoneNumber);

        System.out.println("Generated OTP: " + otp + " for phone number: " + formattedPhoneNumber);

        String message = "Mã OTP của bạn là: " + otp + ". Vui lòng không chia sẻ mã này.";
        long now = Instant.now().toEpochMilli();


        if(formattedPhoneNumber.startsWith("84")) {
            formattedPhoneNumber = "+" + formattedPhoneNumber;
        }
        otpStore.put(formattedPhoneNumber, new OtpEntry(otp, now));
        System.out.println("OTP stored " + otpStore);

        // Này test thử lần rồi nhớ comment phần dưới lại chứ bi tính phí
//            Map<String, MessageAttributeValue> attribute = new HashMap<>();
//
//            // Thiết lập SenderID: Tên người gửi
//            attribute.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
//                    .dataType("String")
//                    .stringValue("Chat")
//                    .build());
//
//            // Thiet lap SMS type
//            attribute.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
//                    .dataType("String")
//                    .stringValue("Transactional") // Transactional: tin nhan giao dịch
//                    .build());
//
//            System.out.println("Sending OTP: " + otp);
//            System.out.println(formattedPhoneNumber);
//
//            // Gửi tin nhắn SMS
//            PublishRequest request = PublishRequest.builder()
//                    .message(message)
//                    .phoneNumber(formattedPhoneNumber)
//                    .messageAttributes(attribute)
//                    .build();
//            System.out.println("Request: " + request);
//
//            PublishResponse response = snsClient.publish(request);
//            System.out.println("Response: " + response);
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        String formattedPhoneNumber = FormatPhoneNumber.formatPhoneNumberTo84(phoneNumber);
        System.out.println("Verifying OTP: " + otp + " for phone number: " + formattedPhoneNumber);
        OtpEntry entry = otpStore.get(formattedPhoneNumber);

        System.out.println(entry);

        if(entry == null) {
            return false; // OTP không tồn tại
        }

        long currentTime = Instant.now().toEpochMilli();
        if(currentTime - entry.getTimestamp() > OTP_TTL_MILLIS) {
            otpStore.remove(formattedPhoneNumber); // Xóa OTP đã hết hạn
            return false;
        }

        if(entry.getOtp().equals(otp)) {
            otpStore.remove(formattedPhoneNumber); // Xóa OTP sau khi xác thực thành công
            return true;
        }

        return false; // OTP không khớp
    }
}
