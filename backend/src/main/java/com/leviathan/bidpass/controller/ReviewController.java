package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Review;
import com.leviathan.bidpass.service.ReviewService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Result<Review> leaveReview(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {

        Integer reviewerId = (Integer) request.getAttribute("userId");
        if (reviewerId == null) {
            return Result.error(401, "Unauthorized");
        }

        Integer orderId = (Integer) body.get("orderId");
        Integer starRating = (Integer) body.get("starRating");
        String comment = (String) body.get("comment");

        if (orderId == null || starRating == null) {
            return Result.error(400, "orderId and starRating are required");
        }

        try {
                Review review = reviewService.leaveReview(reviewerId, orderId,
                    null, starRating, comment);
            return Result.success(review);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> getReviewsForUser(@PathVariable Integer userId) {
        List<Review> reviews = reviewService.getReviewsForUser(userId);
        return Result.success(reviews);
    }

    @GetMapping("/my")
    public Result<List<Review>> getMyReviews(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }
        List<Review> reviews = reviewService.getReviewsByUser(userId);
        return Result.success(reviews);
    }
}