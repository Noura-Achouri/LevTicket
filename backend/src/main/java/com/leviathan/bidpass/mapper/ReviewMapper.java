package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(Review review);

    Review findByOrderIdAndReviewerId(Integer orderId, Integer reviewerId);

    List<Review> findByRevieweeId(Integer revieweeId);

    List<Review> findByReviewerId(Integer reviewerId);
}