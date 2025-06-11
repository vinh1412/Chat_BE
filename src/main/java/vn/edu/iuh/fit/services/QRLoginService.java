package vn.edu.iuh.fit.services;

import com.google.zxing.WriterException;

import java.util.Map;

public interface QRLoginService {
    Map<String, Object> generateQrCode() throws WriterException;
    Map<String, String> verifyQrCode(String sessionId, String username);
    Map<String, String> checkStatus(String sessionId);
}