package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.Order;
import com.leviathan.bidpass.entity.Review;
import com.leviathan.bidpass.mapper.OrderMapper;
import com.leviathan.bidpass.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TrustScoreService trustScoreService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Review leaveReview(Integer reviewerId, Integer orderId,
                              Integer revieweeId, Integer starRating, String comment) {

        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        if (!order.getBuyerId().equals(reviewerId)) {
            throw new RuntimeException("Only the buyer can review the seller");
        }

        if (!order.getStatus().equals("completed") && !order.getStatus().equals("confirmed")) {
            throw new RuntimeException("Order must be completed or confirmed before leaving a review");
        }

        if (reviewMapper.findByOrderIdAndReviewerId(orderId, reviewerId) != null) {
            throw new RuntimeException("You have already reviewed this order");
        }

        if (starRating < 1 || starRating > 5) {
            throw new RuntimeException("Star rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(order.getSellerId());
        review.setStarRating(starRating);
        review.setComment(comment);

        reviewMapper.insert(review);
        trustScoreService.updateAfterReview(order.getSellerId(), starRating);
        notificationService.create(order.getSellerId(), "review", "New buyer review",
            "A buyer left you a " + starRating + "-star review.", orderId.longValue());
        return review;
    }

    public List<Review> getReviewsForUser(Integer userId) {
        return reviewMapper.findByRevieweeId(userId);
    }

    public List<Review> getReviewsByUser(Integer userId) {
        return reviewMapper.findByReviewerId(userId);
    }
}