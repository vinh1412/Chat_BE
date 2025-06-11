/**
 * @ (#) LoginLogRepository.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.LoginLog;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
@Repository
public interface LoginLogRepository extends MongoRepository<LoginLog, ObjectId> {

    LoginLog findFirstByUserIdOrderByLoginTimeDesc(ObjectId userId); // tim kiem login log cuoi cung cua user
}
