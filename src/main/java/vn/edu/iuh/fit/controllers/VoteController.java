/**
 * @ (#) VoteXController.java      5/27/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.Vote;
import vn.edu.iuh.fit.services.VoteService;

import java.util.List;
import java.util.Optional;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/27/2025
 */
@RestController
@RequestMapping("/api/v1/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping("/create")
    public ResponseEntity<Vote> createVote(@RequestBody Vote vote) {
        Vote createdVote = voteService.createVote(vote);
        return ResponseEntity.ok(createdVote);
    }
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Vote> getLatestVoteByGroup(@PathVariable String groupId) {
        ObjectId objId = new ObjectId(groupId);
        Optional<Vote> voteOpt = voteService.getLatestVoteByGroupId(objId);
        return voteOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/voteoption")
    public ResponseEntity<Vote> voteOption(
            @RequestParam String voteId,
            @RequestParam String questionContent,
            @RequestParam String userId
    ) {
        Optional<Vote> updatedVoteOpt = voteService.updateVoteUserIds(
                new ObjectId(voteId),
                questionContent,
                new ObjectId(userId)
        );
        return updatedVoteOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add question to vote
    @PostMapping("/add-question")
    public ResponseEntity<Vote> addQuestionToVote(
            @RequestParam String voteId,
            @RequestParam String questionContent
    ) {
        Optional<Vote> updatedVoteOpt = voteService.addQuestionToVote(new ObjectId(voteId), questionContent);
        return updatedVoteOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get vote by ID
    @GetMapping("/{voteId}")
    public ResponseEntity<Vote> getVoteById(@PathVariable String voteId) {
        Optional<Vote> voteOpt = voteService.getVoteById(new ObjectId(voteId));
        return voteOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


}