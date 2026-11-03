package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Bid;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BidMapper {

    void insert(Bid bid);

    List<Bid> findByAuctionId(Integer auctionId);

    Bid findHighestBid(Integer auctionId);

    List<Map<String, Object>> findSellerBidStats(Integer sellerId);
}