package vn.edu.iuh.fit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.services.QRLoginService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/qr")
public class QRLoginController {

    @Autowired
    private QRLoginService qrLoginService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQrCode() throws Exception {
        return ResponseEntity.ok(qrLoginService.generateQrCode());
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyQrCode(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String userId = request.get("userId");

        // Gọi service để xác thực theo userId
        return ResponseEntity.ok(qrLoginService.verifyQrCode(sessionId, userId));
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Map<String, String>> checkStatus(@PathVariable String sessionId) {
        return ResponseEntity.ok(qrLoginService.checkStatus(sessionId));
    }


}
