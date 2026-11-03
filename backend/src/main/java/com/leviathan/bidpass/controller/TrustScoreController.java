package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.TrustScore;
import com.leviathan.bidpass.service.TrustScoreService;
import com.leviathan.bidpass.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class TrustScoreController {

    @Autowired
    private TrustScoreService trustScoreService;

    @GetMapping("/{id}/trust-score")
    public Result<TrustScore> getTrustScore(@PathVariable Integer id) {
        TrustScore trustScore = trustScoreService.getTrustScore(id);
        return Result.success(trustScore);
    }
}