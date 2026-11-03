package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    Order findById(Integer id);

    Order findByAuctionId(Integer auctionId);

    List<Order> findByBuyerId(Integer buyerId);

    List<Order> findBySellerId(Integer sellerId);

    void updateStatus(@Param("id") Integer id, @Param("status") String status);
}