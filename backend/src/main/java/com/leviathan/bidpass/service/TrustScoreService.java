package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.TrustScore;
import com.leviathan.bidpass.mapper.TrustScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrustScoreService {

    @Autowired
    private TrustScoreMapper trustScoreMapper;

    public TrustScore getOrCreateTrustScore(Integer userId) {
        TrustScore trustScore = trustScoreMapper.findByUserId(userId);
        if (trustScore == null) {
            trustScore = new TrustScore();
            trustScore.setUserId(userId);
            trustScoreMapper.insert(trustScore);
        }
        return trustScore;
    }

    public TrustScore getTrustScore(Integer userId) {
        return getOrCreateTrustScore(userId);
    }

    @Transactional
    public void updateAfterCompletedOrder(Integer userId, boolean isDisputed) {
        TrustScore trustScore = getOrCreateTrustScore(userId);

        int total = (trustScore.getTotalAuctions() != null ? trustScore.getTotalAuctions() : 0) + 1;
        int successful = isDisputed
            ? (trustScore.getSuccessfulAuctions() != null ? trustScore.getSuccessfulAuctions() : 0)
            : (trustScore.getSuccessfulAuctions() != null ? trustScore.getSuccessfulAuctions() : 0) + 1;
        int disputes = isDisputed
            ? (trustScore.getDisputes() != null ? trustScore.getDisputes() : 0) + 1
            : (trustScore.getDisputes() != null ? trustScore.getDisputes() : 0);

    int score = (successful * 2) - (disputes * 3);
    if (score < 0) score = 0;

    trustScoreMapper.updateScore(userId, score, total, successful, disputes);
   }

    @Transactional
    public void updateAfterDispute(Integer userId) {
         TrustScore trustScore = getOrCreateTrustScore(userId);

         int disputes = (trustScore.getDisputes() != null ? trustScore.getDisputes() : 0) + 1;
         int successful = trustScore.getSuccessfulAuctions() != null ? trustScore.getSuccessfulAuctions() : 0;
         int total = trustScore.getTotalAuctions() != null ? trustScore.getTotalAuctions() : 0;

         int score = (successful * 2) - (disputes * 3);
        if (score < 0) score = 0;

        trustScoreMapper.updateScore(userId, score, total, successful, disputes);
    }

    @Transactional
    public void updateAfterReview(Integer userId, Integer starRating) {
        TrustScore trustScore = getOrCreateTrustScore(userId);
        int adjustment = (starRating - 3) * 5;
        int score = Math.max(0, Math.min(100, trustScore.getScore() + adjustment));
        trustScoreMapper.adjustScore(userId, score);
    }
}