package vn.edu.iuh.fit.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.QRLoginService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QRLoginServiceImpl implements QRLoginService {

    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, Object> generateQrCode() throws WriterException {
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, "PENDING");

        // Timeout sau 5 phút
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000); // 5 phút
                sessionStore.remove(sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(sessionId, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrImage, "png", baos);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("qrCode", "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray()));
        return response;
    }

    @Override
    public Map<String, String> verifyQrCode(String sessionId, String username) {
        if (!sessionStore.containsKey(sessionId) || !sessionStore.get(sessionId).equals("PENDING")) {
            return Map.of("status", "INVALID");
        }
        // Tìm người dùng theo id thay vì username
        Optional<User> userOptional = userRepository.findById(new ObjectId(username)); // assuming username is the ObjectId in this case

        if (userOptional.isEmpty()) {
            return Map.of("status", "USER_NOT_FOUND");
        }

        String userId = String.valueOf(userOptional.get().getId());

        String jwtToken = Jwts.builder()
                .setSubject(userId)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        sessionStore.put(sessionId, "APPROVED:" + jwtToken);

        return Map.of("status", "APPROVED");
    }

    @Override
    public Map<String, String> checkStatus(String sessionId) {
        String status = sessionStore.getOrDefault(sessionId, "INVALID");
        Map<String, String> response = new HashMap<>();
        if (status.startsWith("APPROVED:")) {
            response.put("status", "APPROVED");
            response.put("token", status.split(":")[1]);
        } else {
            response.put("status", status);
        }
        return response;
    }
}
