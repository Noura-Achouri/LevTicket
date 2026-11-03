
package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper {

    void insert(Ticket ticket);

    Ticket findById(Integer id);

    List<Ticket> findAll(@Param("keyword") String keyword,
                         @Param("categoryId") Integer categoryId,
                         @Param("location") String location,
                         @Param("tag") String tag,
                         @Param("sort") String sort,
                         @Param("offset") int offset,
                         @Param("size") int size);

    int countAll(@Param("keyword") String keyword,
                 @Param("categoryId") Integer categoryId,
                 @Param("location") String location,
                 @Param("tag") String tag);

    List<Ticket> findBySellerId(@Param("sellerId") Integer sellerId,
                                @Param("status") String status);

    List<String> findImageUrlsByTicketId(Integer ticketId);

    List<String> findPrivateImageUrlsByTicketId(Integer ticketId);

    void insertImage(@Param("ticketId") Integer ticketId, @Param("imageUrl") String imageUrl,
                     @Param("isPrivate") boolean isPrivate);

    int deleteByIdAndSellerId(@Param("id") Integer id, @Param("sellerId") Integer sellerId);

    void updateStatus(@Param("id") Integer id, @Param("status") String status);
}