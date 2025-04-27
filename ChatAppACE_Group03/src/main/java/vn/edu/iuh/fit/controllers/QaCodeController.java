package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.QaCode;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.services.QaCodeService;

@RestController
@RequestMapping("/api/v1/qacode")
public class QaCodeController {

    @Autowired
    private QaCodeService qaCodeService;

    @PostMapping("/save")
    public ResponseEntity<QaCode> saveQaCode(@RequestParam("sessionId") String sessionId,
                                           @RequestParam("userId") ObjectId userId, @RequestParam("token") String token) {
        Boolean status = true; // Trạng thái mặc định là true
        QaCode qa = qaCodeService.saveQaCode(sessionId, status, userId, token);
        if (qa == null) {
            return ResponseEntity.badRequest().build(); // Trả về lỗi nếu không thể lưu
        }
        return ResponseEntity.ok(qa); // Trả về đối tượng QaCode đã lưu

    }


    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Boolean> checkStatus(@PathVariable("sessionId") String sessionId) {
        Boolean status = qaCodeService.checkStatus(sessionId);
        if (status == null) {
            return ResponseEntity.notFound().build(); // mã QR không tồn tại
        }
        return ResponseEntity.ok(status);
    }


    // http:// localhost:8080/api/v1/qacode/42bdd7a5-0e74-44e9-81c2-04017b9d02be
    // TIM KIEM IDUSER THEO SESSIONID
    @GetMapping("/{sessionId}")
    public ResponseEntity<QaCode> findUserBySessionId(@PathVariable("sessionId") String sessionId) {
        System.out.println("sessionId: " + sessionId);
        QaCode qaCode = qaCodeService.findUserIdBySessionId(sessionId);
        if (qaCode == null) {
            return ResponseEntity.notFound().build(); // mã QR không tồn tại
        }
        return ResponseEntity.ok(qaCode); // Trả về đối tượng QaCode đã tìm thấy
    }

}

