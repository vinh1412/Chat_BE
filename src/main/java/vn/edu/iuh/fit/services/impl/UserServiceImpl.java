/*
 * @ {#} UserServiceImpl.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.InvalidPasswordException;
import vn.edu.iuh.fit.exceptions.UserNotFoundException;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.JwtTokenUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtDecoder  jwtDecoder;


    private UserResponse convertToDto(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
    @Override
    public UserResponse getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() ->new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));
        return convertToDto(user);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public boolean isPasswordValid(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu không chính xác.");
        }

        return true;
    }

    @Override
    public void updatePassword(String phone, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser(String token) {
        String jwt = token.replace("Bearer ", "").trim(); // loại bỏ tiền tố "Bearer "

        Jwt jwtToken = this.jwtDecoder.decode(jwt); // decode đúng JWT
        String phone = jwtTokenUtil.getPhoneFromToken(jwtToken); // lấy số điện thoại từ JWT

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));

        return this.convertToDto(user);
    }

    @Override
    public UserResponse updateUser(ObjectId userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);

        return this.convertToDto(user);
    }

    @Override
    public UserResponse changePassword(ObjectId userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu cũ không đúng.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return convertToDto(user);
    }

    @Override
    public List<UserResponse> searchByKeyWord(String keyWord) {
        if(keyWord.startsWith("0")) {
            keyWord = "84" + keyWord.substring(1);
        }
        System.out.println("KeyWord: " + keyWord);

        List<User> search = userRepository.findByKeyWord(keyWord);

        if(search.isEmpty()) {
         return  Collections.emptyList();
        }

        return search.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByIds(Set<ObjectId> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(ObjectId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return convertToDto(user);
    }

    @Override
    public Optional<User> getUserByIds(ObjectId userId) {
        return userRepository.findById(userId);
    }


}
