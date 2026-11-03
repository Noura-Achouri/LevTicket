package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.TrustScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrustScoreMapper {

    void insert(TrustScore trustScore);

    TrustScore findByUserId(Integer userId);

    void updateScore(@Param("userId") Integer userId,
                     @Param("score") Integer score,
                     @Param("totalAuctions") Integer totalAuctions,
                     @Param("successfulAuctions") Integer successfulAuctions,
                     @Param("disputes") Integer disputes);

    void adjustScore(@Param("userId") Integer userId, @Param("score") Integer score);
}