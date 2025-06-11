/*
 * @ {#} TokenRepository.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.RefreshToken;

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, ObjectId> {
    Optional<RefreshToken> findByRefreshToken(String token);
    RefreshToken findTopByUserIdAndRevokedFalseOrderByExpiresDateDesc(ObjectId userId);
    List<RefreshToken> findAllByUserIdAndRevokedFalse(ObjectId userId);
}
