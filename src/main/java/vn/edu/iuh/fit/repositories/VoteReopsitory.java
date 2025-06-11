/**
 * @ (#) VoteReopsitory.java      5/27/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import vn.edu.iuh.fit.entities.Vote;
import org.springframework.data.mongodb.repository.Query;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/27/2025
 */
public interface VoteReopsitory extends MongoRepository<Vote, ObjectId> {

    Optional<Vote> findFirstByGroupIdAndCreatedAtBeforeOrderByCreatedAtDesc(ObjectId groupId, Instant now);

}