package com.leviathan.bidpass.service;

import com.leviathan.bidpass.entity.User;
import com.leviathan.bidpass.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import com.leviathan.bidpass.mapper.BidMapper;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BidMapper bidMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(String username, String email, String password, String phone) {
        if (userMapper.findByEmail(email) != null) {
            throw new RuntimeException("Email already in use");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);

        userMapper.insert(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Wrong password");
        }
        return user;
    }

    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    public Map<String, Object> findTrustScore(Integer userId) {
        return userMapper.findTrustScore(userId);
    }

    public User updateProfile(Integer userId, String bio, String phone) {
    User user = userMapper.findById(userId);
    if (user == null) {
        throw new RuntimeException("User not found");
    }
    if (bio != null) user.setBio(bio);
    if (phone != null) user.setPhone(phone);
    userMapper.updateProfile(user);
    return user;
    } 

    public User updateAvatar(Integer userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        return userMapper.findById(userId);
    }

    public List<Map<String, Object>> findSellerBidStats(Integer userId) {
        return bidMapper.findSellerBidStats(userId);
    }
}