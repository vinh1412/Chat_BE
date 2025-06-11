/*
 * @ {#} AuthController.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ChangePasswordRequest;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.RefreshTokenResponse;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.exceptions.MissingTokenException;
import vn.edu.iuh.fit.exceptions.UserAlreadyExistsException;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.SmsService;
import vn.edu.iuh.fit.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private Validator validator;

    //    Request body:
/*
    {
            "display_name":"Anh Dat",
            "phone":"+84036154587",
            "password":"12345678",
            "dob": "2003-01-15",
            "roles":[
            ]
    }
*/

    /**
     * test api trong postman
     * phan header: Content-Type: multipart/form-data
     * phan body: form-data (thay vi chon raw thi chon form-data)
     * chon key 1 la signUpRequest, value la text(nhap theo dinh dang json) cua product
     * chon key 2 la avatar, value la file (chon file anh)
     *
     * Vi khong su dụng truc tiep @Valid nen phai dung Validator de validate
     *
     * @param signUpRequestJson
     * @param avatar
     * @return
     */
    @PostMapping(value = "/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> signUp(
            @RequestPart("signUpRequest")  String signUpRequestJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        System.out.println("signUpRequestJson: " + signUpRequestJson);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SignUpRequest signUpRequest;
        try {
            signUpRequest = objectMapper.readValue(signUpRequestJson, SignUpRequest.class);
            String avatarUrl = null;
            if(avatar != null && !avatar.isEmpty()) {
                avatarUrl = cloudinaryService.uploadImage(avatar);
                signUpRequest.setAvatarUrl(avatarUrl);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Invalid request format: " + e.getMessage())
                    .build());
        }


        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(signUpRequest);
        if(!violations.isEmpty()) {
            Map<String, Object> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Validation failed")
                    .response(errors)
                    .build());
        }

        authService.signUp(signUpRequest);

        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .response(null)
                .message("Sign up successfully!")
                .build());
    }

    //    Request body:
/*
    {
        "phone":"phoneNumber",
        "password":"password"
    }
*/
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@RequestBody @Valid SignInRequest signInRequest) {
        SignInResponse signInResponse = authService.signIn(signInRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .response(signInResponse)
                .message("Sign in successfully!")
                .build());
    }

    // Authorization: Bearer Token <access_token>
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .response(null)
                .message("Logout successfully!")
                .build());
    }

    // Request body:
/*
        {
            "refreshToken": <refresh_token>
        }
*/
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new MissingTokenException("Missing Refresh Token in request!");
        }

        RefreshTokenResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.builder()
                .status("SUCCESS")
                .response(response)
                .message("Token refreshed successfully!")
                .build());
    }

    // Request body:
/*
        {
            "idToken": <id_token>
        }
 */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody Map<String, String> request) {

        String idToken = request.get("idToken");

        if (idToken == null || idToken.isEmpty()) {
            throw new MissingTokenException("Thiếu ID Token trong request!");
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String phoneNumber = decodedToken.getClaims().get("phone_number").toString();

            // Kiểm tra xem số điện thoại đã tồn tại chưa
            if (userService.existsByPhone(phoneNumber)) {
                throw new UserAlreadyExistsException("Số điện thoại đã được sử dụng!");
            }

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Xác thực thành công! Số điện thoại: " + phoneNumber)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Xác thực OTP thất bại: " + e.getMessage())
                    .build());
        }
    }
    // Request body:
/*
    {
        "idToken": "<firebase_id_token>",
        "newPassword": "yourNewPassword123"
    }
*/
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        String newPassword = request.get("newPassword");

        if (idToken == null || idToken.isEmpty()) {
            throw new MissingTokenException("Thiếu ID Token trong request!");
        }

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Mật khẩu mới phải có ít nhất 8 ký tự!")
                    .build());
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            System.out.println("Decoded Token: " + decodedToken.getClaims());
            String phoneNumber = decodedToken.getClaims().get("phone_number").toString();


            // Kiểm tra xem số điện thoại đã tồn tại chưa
            if (!userService.existsByPhone(phoneNumber)) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("ERROR")
                        .message("Số điện thoại không tồn tại!")
                        .build());
            }

            // Cập nhật mật khẩu
            userService.updatePassword(phoneNumber, newPassword);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Đặt lại mật khẩu thành công cho số điện thoại: " + phoneNumber)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Đặt lại mật khẩu thất bại: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            smsService.sendOtp(phoneNumber);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("OTP đã được gửi đến số điện thoại: " + phoneNumber)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Gửi OTP thất bại: " + e.getMessage())
                    .build());
        }

    }

    @PostMapping("/verify-otp-sns")
    public ResponseEntity<ApiResponse<?>> verifyOtp_Sns(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String otp = request.get("otp");

        boolean isValid = smsService.verifyOtp(phoneNumber, otp);
        if(isValid) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Xác thực thành công! Số điện thoại: " + phoneNumber)
                    .build());
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Xác thực OTP thất bại!")
                    .build());
        }
    }

    @PostMapping("/reset-password-mobile")
    public ResponseEntity<ApiResponse<?>> resetPasswordMobile(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String newPassword = request.get("newPassword");

        System.out.println("Request reset password: " + request.toString() + " phoneNumber: " + phoneNumber + " newPassword: " + newPassword);

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Mật khẩu mới phải có ít nhất 8 ký tự!")
                    .build());
        }

        try {

            // Kiểm tra xem số điện thoại đã tồn tại chưa
            if (!userService.existsByPhone(phoneNumber)) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("ERROR")
                        .message("Số điện thoại không tồn tại!")
                        .build());
            }

            // Cập nhật mật khẩu
            userService.updatePassword(phoneNumber, newPassword);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Đặt lại mật khẩu thành công cho số điện thoại: " + phoneNumber)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Đặt lại mật khẩu thất bại: " + e.getMessage())
                    .build());
        }
    }
}
