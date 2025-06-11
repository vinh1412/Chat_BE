/**
 * @ (#) VoteService.java      5/27/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.Vote;

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/27/2025
 */
public interface VoteService {
    Vote createVote(Vote vote);
    Optional<Vote> getLatestVoteByGroupId(ObjectId groupId);
    Optional<Vote> updateVoteUserIds(ObjectId voteId, String questionContent, ObjectId userId);

    // add question to vote
    Optional<Vote> addQuestionToVote(ObjectId voteId, String questionContent);

    // lay thong tin vote theo voteId
    Optional<Vote> getVoteById(ObjectId voteId);
}