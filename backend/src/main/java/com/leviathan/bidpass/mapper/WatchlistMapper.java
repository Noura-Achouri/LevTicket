package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WatchlistMapper {

    int insert(@Param("userId") Integer userId, @Param("ticketId") Integer ticketId);

    int delete(@Param("userId") Integer userId, @Param("ticketId") Integer ticketId);

    int exists(@Param("userId") Integer userId, @Param("ticketId") Integer ticketId);

    List<Integer> findTicketIdsByUserId(Integer userId);

    List<Ticket> findWatchedTickets(Integer userId);

    int countByTicketId(Integer ticketId);

    List<Integer> findWatcherIdsByTicketId(Integer ticketId);
}
