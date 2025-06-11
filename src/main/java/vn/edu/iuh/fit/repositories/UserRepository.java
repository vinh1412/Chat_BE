/*
 * @ {#} UserRepository.java   1.0     15/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.User;

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   15/03/2025
 * @version:    1.0
 */
@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> { ;
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findById(ObjectId id);

    @Query("{'$or': [{'displayName': {'$regex': ?0, '$options': 'i'}}, {'phone': {'$regex': ?0, '$options': 'i'}}]}")
    List<User> findByKeyWord(String keyWord);
}
