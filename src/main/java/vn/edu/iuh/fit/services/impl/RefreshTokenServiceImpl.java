/*
 * @ {#} TokenServiceImpl.java   1.0     17/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.RefreshToken;
import vn.edu.iuh.fit.exceptions.TokenNotFoundException;
import vn.edu.iuh.fit.repositories.RefreshTokenRepository;
import vn.edu.iuh.fit.services.RefreshTokenService;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/03/2025
 * @version:    1.0
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Override
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token).orElseThrow(() -> new TokenNotFoundException("Không tìm thấy refresh token: " + token));
    }

    @Override
    public boolean isTokenRevoke(String token) {
        return refreshTokenRepository.findByRefreshToken(token)
                .map(RefreshToken::isRevoked)
                .orElse(true);
    }

    @Override
    public String getRefreshTokenByUser(ObjectId userId) {
        RefreshToken refreshToken = refreshTokenRepository.findTopByUserIdAndRevokedFalseOrderByExpiresDateDesc(userId);
        if (refreshToken == null) return null;
        System.out.println("Token: " + refreshToken.getRefreshToken());
        return refreshToken != null ? refreshToken.getRefreshToken() : null;
    }

    @Override
    public List<RefreshToken> getValidTokensByUserId(ObjectId userId) {
        return refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
    }
}
