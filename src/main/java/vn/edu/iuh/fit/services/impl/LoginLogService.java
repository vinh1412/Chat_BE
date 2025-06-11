/**
 * @ (#) LoginLogService.java      4/8/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.LoginLog;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.LoginStatus;
import vn.edu.iuh.fit.repositories.LoginLogRepository;
import vn.edu.iuh.fit.repositories.UserRepository;

import java.time.Instant;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/8/2025
 */
@Service
public class LoginLogService implements vn.edu.iuh.fit.services.LoginLogService {
    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void saveLogin(ObjectId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        LoginLog loginLog = LoginLog.builder()
                .userId(user.getId())
                .loginTime(Instant.now())
                .status(LoginStatus.ONLINE)
                .build();
        loginLogRepository.save(loginLog);
    }

    @Override
    public void saveLogout(ObjectId userId) {
        LoginLog lastLoginLog = loginLogRepository.findFirstByUserIdOrderByLoginTimeDesc(userId);

        if (lastLoginLog != null) {
            lastLoginLog.setLogoutTime(Instant.now());
            lastLoginLog.setStatus(LoginStatus.OFFLINE);
            loginLogRepository.save(lastLoginLog);
        }
        else {
            throw new RuntimeException("No login log found for user");
        }

    }
}
