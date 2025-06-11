/*
 * @ {#} TokenService.java   1.0     17/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.RefreshToken;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/03/2025
 * @version:    1.0
 */
public interface RefreshTokenService {
    void saveRefreshToken(RefreshToken refreshToken);
    RefreshToken findByToken(String token);
    boolean isTokenRevoke(String token);
    String getRefreshTokenByUser(ObjectId userId);
    List<RefreshToken> getValidTokensByUserId(ObjectId userId);
}
