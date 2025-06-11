/**
 * @ (#) VoteServiceImpl.java      5/27/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Question;
import vn.edu.iuh.fit.entities.Vote;
import vn.edu.iuh.fit.repositories.VoteReopsitory;
import vn.edu.iuh.fit.services.VoteService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/27/2025
 */
@Service

public class VoteServiceImpl implements VoteService {

    @Autowired
    private VoteReopsitory voteReopsitory;


    @Override
    public Vote createVote(Vote vote) {
        if (vote.getCreatedAt() == null) {
            vote.setCreatedAt(Instant.now());
        }
        return voteReopsitory.save(vote);
    }

    @Override
    public Optional<Vote> getLatestVoteByGroupId(ObjectId groupId) {
        Instant now = Instant.now();
        return voteReopsitory.findFirstByGroupIdAndCreatedAtBeforeOrderByCreatedAtDesc(groupId, now);
    }

    @Override
    public Optional<Vote> updateVoteUserIds(ObjectId voteId, String questionContent, ObjectId userId) {
        Optional<Vote> voteOpt = voteReopsitory.findById(voteId);
        if (voteOpt.isEmpty()) return Optional.empty();

        Vote vote = voteOpt.get();
        List<Question> questions = vote.getQuestions();
        if (questions == null || questions.isEmpty()) return Optional.empty();

        Question question = questions.stream()
                .filter(q -> q.getContent().equals(questionContent))
                .findFirst()
                .orElse(null);
        if (question == null) return Optional.empty();

        Set<ObjectId> userIds = question.getUserIds();
        if (userIds == null) {
            userIds = new HashSet<>();
            question.setUserIds(userIds);
        }

        if (!vote.isMultipleChoice()) {
            for (Question q : questions) {
                if (!q.equals(question) && q.getUserIds() != null) {
                    q.getUserIds().remove(userId);
                }
            }
        }

        if (userIds.contains(userId)) {
            userIds.remove(userId);
        } else {
            userIds.add(userId);
        }

        vote.setQuestions(questions);

        Vote updatedVote = voteReopsitory.save(vote);
        return Optional.of(updatedVote);
    }

    @Override
    public Optional<Vote> addQuestionToVote(ObjectId voteId, String questionContent) {
        // kiem tra neu isAddOption = true
        Optional<Vote> voteOpt = voteReopsitory.findById(voteId);
        if (voteOpt.isEmpty()) return Optional.empty();
        Vote vote = voteOpt.get();
        if (!vote.isAddOption()) {
            return Optional.empty(); // Khong cho phep them cau hoi neu isAddOption = false
        }
        List<Question> questions = vote.getQuestions();
        if (questions == null) {
            questions = List.of();
        }
        Question newQuestion = new Question();
        newQuestion.setContent(questionContent);
        Set<ObjectId> userIds = new HashSet<>();
        newQuestion.setUserIds(userIds);
        questions.add(newQuestion);
        vote.setQuestions(questions);
        vote.setCreatedAt(Instant.now()); // Cap nhat thoi gian tao moi
        Vote updatedVote = voteReopsitory.save(vote);
        return Optional.of(updatedVote);
    }

    @Override
    public Optional<Vote> getVoteById(ObjectId voteId) {
        return voteReopsitory.findById(voteId);
    }


}