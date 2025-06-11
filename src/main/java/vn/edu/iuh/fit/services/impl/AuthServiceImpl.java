/*
 * @ {#} AuthServiceImpl.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.auths.UserPrincipal;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.RefreshTokenResponse;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.entities.RefreshToken;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.*;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.LoginLogService;
import vn.edu.iuh.fit.services.RefreshTokenService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;
import vn.edu.iuh.fit.utils.JwtTokenUtil;

import java.util.ArrayList;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private LoginLogService loginLogService;

    @Override
    public boolean signUp(SignUpRequest signUpRequest) {
        if (userService.existsByPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.getPhone()))) {
            throw new UserAlreadyExistsException("Số điện thoại đã được đăng ký.");
        }
        User user = createUser(signUpRequest);
        userService.save(user);
        return true;
    }

    private User createUser(SignUpRequest signUpRequest) {
        String rawPassword = signUpRequest.getPassword();
        if (!rawPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ cái và số.");
        }
        List<String> assignedRoles = determineRoles(signUpRequest.getRoles());
        System.out.println("Assigned roles: " + assignedRoles);
        return User.builder()
                .displayName(signUpRequest.getDisplayName())
                .phone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.getPhone()))
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .enabled(true)
                .dob(signUpRequest.getDob())
                .avatar(signUpRequest.getAvatarUrl())
                .gender(signUpRequest.getGender())
                .roles(assignedRoles)
                .build();
    }

    private List<String> determineRoles(List<String> strRoles) {
        List<String> roles = new ArrayList<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add("ROLE_USER");
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        roles.add("ROLE_ADMIN");
                        break;
                    default:
                        roles.add("ROLE_USER");
                }
            });
        }
        return roles;
    }

    @Override
    public SignInResponse signIn(SignInRequest signInRequest) {
        String formattedPhone = FormatPhoneNumber.formatPhoneNumberTo84(signInRequest.getPhone());
        if (!userService.existsByPhone(formattedPhone)) {
            throw new UserNotFoundException("Số điện thoại không tồn tại.");
        }

        userService.isPasswordValid(formattedPhone, signInRequest.getPassword());

        // Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(formattedPhone, signInRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtTokenUtil.generateToken(authentication, jwtEncoder);
        String refreshToken = jwtTokenUtil.generateRefreshToken(authentication, jwtEncoder);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ObjectId userId = userPrincipal.getUserResponse().getId();


        RefreshToken token = RefreshToken.builder()
                .refreshToken(refreshToken)
                .userId(userId)
                .expiresDate(jwtTokenUtil.generateExpirationDate())
                .revoked(false)
                .build();

        // Lưu thông tin đăng nhập vào lịch sử
        loginLogService.saveLogin(userId);

        refreshTokenService.saveRefreshToken(token);

        return SignInResponse.builder()
                .id(userId.toHexString())
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .phone(userPrincipal.getUserResponse().getPhone())
                .roles(userPrincipal.getAuthorities())
                .build();
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new MissingTokenException("Token không hợp lệ hoặc không tồn tại.");
        }

        String jwtToken = accessToken.substring(7);
        try {
            Jwt decodedToken = jwtDecoder.decode(jwtToken);
            String userName = decodedToken.getSubject();

            if (userName == null || userName.isEmpty()) {
                throw new IllegalArgumentException("Token không chứa thông tin hợp lệ.");
            }

            boolean isAccessToken = decodedToken.getClaims().containsKey("roles");
            if (!isAccessToken) {
                throw new InvalidTokenException("Bạn đã truyền nhầm Refresh Token thay vì Access Token.");
            }

            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(userName);

            ObjectId userId = userPrincipal.getUserResponse().getId();
            if (userId == null) {
                throw new TokenRevokedException("ID người dùng không hợp lệ.");
            }

            // Lấy refreshToken của user từ DB
            String refreshToken = refreshTokenService.getRefreshTokenByUser(userId);
            if (refreshToken == null) {
                throw new IllegalArgumentException("Không tìm thấy refresh token cho người dùng này.");
            }

            RefreshToken storedRefreshToken = refreshTokenService.findByToken(refreshToken);

            storedRefreshToken.setRevoked(true);  // Thu hồi refresh token
            loginLogService.saveLogout(userId); // Lưu thông tin đăng xuất vào lịch sử

            refreshTokenService.saveRefreshToken(storedRefreshToken);


            SecurityContextHolder.clearContext();
        } catch (JwtException e) {
            throw new InvalidTokenException("Token không hợp lệ.");
        }
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new MissingTokenException("Refresh token không được để trống.");
        }

        try {
            Jwt decodedToken = jwtDecoder.decode(refreshToken);
            String username = decodedToken.getSubject();

            // Kiểm tra token có bị thu hồi không
            RefreshToken storedRefreshToken = refreshTokenService.findByToken(refreshToken);
            if (storedRefreshToken == null || storedRefreshToken.isRevoked()) {
                throw new TokenRevokedException("Refresh token đã bị thu hồi. Vui lòng đăng nhập lại.");
            }

            // Lấy danh sách refresh token hợp lệ của user
            List<RefreshToken> validRefreshTokens = refreshTokenService.getValidTokensByUserId(storedRefreshToken.getUserId());

            // Nếu user không còn refresh token hợp lệ => buộc đăng xuất
            if (validRefreshTokens.isEmpty()) {
                throw new TokenRevokedException("Không còn refresh token hợp lệ. Vui lòng đăng nhập lại.");
            }

            // Lấy user từ database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails == null) {
                throw new UserNotFoundException("Không tìm thấy người dùng từ token.");
            }

            UserPrincipal userPrincipal = (UserPrincipal) userDetails;

            // Tạo access token mới
            String newAccessToken = jwtTokenUtil.generateToken(
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities()),
                    jwtEncoder
            );

            return new RefreshTokenResponse(newAccessToken);
        } catch (JwtException  e) {
            throw new InvalidTokenException("Refresh token không hợp lệ.");
        }
    }
}
