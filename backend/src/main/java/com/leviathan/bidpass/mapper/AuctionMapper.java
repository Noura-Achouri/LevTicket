package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Auction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AuctionMapper {

    void insert(Auction auction);

    Auction findById(Integer id);

    Auction findByTicketId(Integer ticketId);

    /** Auctions whose end time has passed but that are still marked 'ongoing'. */
    List<Auction> findExpiredOngoing();

    void updateCurrentBid(@Param("id") Integer id,
                          @Param("currentBid") BigDecimal currentBid,
                          @Param("winnerId") Integer winnerId);

    void closeAuction(@Param("id") Integer id,
                      @Param("winnerId") Integer winnerId);
}