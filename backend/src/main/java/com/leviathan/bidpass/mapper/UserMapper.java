package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper {

    void insert(User user);

    User findByEmail(String email);

    User findByUsername(String username);

    User findById(Integer id);

    Map<String, Object> findTrustScore(Integer userId);

    void updateProfile(User user);

    void updateAvatar(@org.apache.ibatis.annotations.Param("id") Integer id,
                      @org.apache.ibatis.annotations.Param("avatarUrl") String avatarUrl);
}