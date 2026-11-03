package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.User;
import com.leviathan.bidpass.service.UserService;
import com.leviathan.bidpass.service.ReviewService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/me")
    public Result<Map<String, Object>> getMyProfile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return buildProfile(userId, true);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Integer id) {
        return buildProfile(id, false);
    }

    @GetMapping("/{id}/reviews")
    public Result<java.util.List<com.leviathan.bidpass.entity.Review>> getUserReviews(@PathVariable Integer id) {
        if (userService.findById(id) == null) return Result.error(404, "User not found");
        return Result.success(reviewService.getReviewsForUser(id));
    }

    private Result<Map<String, Object>> buildProfile(Integer id, boolean includePrivateFields) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        if (includePrivateFields) {
            data.put("email", user.getEmail());
            data.put("phone", user.getPhone());
        }
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("bio", user.getBio());
        data.put("verificationStatus", user.getVerificationStatus());
        data.put("createTime", user.getCreateTime());
        data.put("reviews", reviewService.getReviewsForUser(id));
        Map<String, Object> trustScore = userService.findTrustScore(id);
        data.put("trustScore", trustScore != null ? trustScore : Map.of("score", 0, "totalAuctions", 0, "successfulAuctions", 0));
        return Result.success(data);
    }

    @PutMapping("/me")
    public Result<Map<String, Object>> updateProfile(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        String bio = body.get("bio");
        String phone = body.get("phone");

        User updated = userService.updateProfile(userId, bio, phone);
        Map<String, Object> data = new HashMap<>();
        data.put("id", updated.getId());
        data.put("username", updated.getUsername());
        data.put("bio", updated.getBio());
        data.put("phone", updated.getPhone());
        return Result.success(data);
    }

    @PostMapping("/me/avatar")
    public Result<Map<String, String>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("avatar") MultipartFile avatar) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        if (avatar == null || avatar.isEmpty()) return Result.error(400, "Avatar image is required");

        try {
            Path uploadDirectory = Paths.get("uploads", "avatars");
            Files.createDirectories(uploadDirectory);
            String originalName = avatar.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.')) : ".bin";
            Path destination = uploadDirectory.resolve(userId + "-" + UUID.randomUUID() + extension);
            avatar.transferTo(destination);

            String avatarUrl = "/uploads/avatars/" + destination.getFileName();
            userService.updateAvatar(userId, avatarUrl);
            return Result.success(Map.of("avatarUrl", avatarUrl));
        } catch (Exception e) {
            return Result.error(400, "Failed to save avatar: " + e.getMessage());
        }
    }

    @DeleteMapping("/me/avatar")
    public Result<Map<String, String>> deleteAvatar(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        userService.updateAvatar(userId, null);
        return Result.success();
    }

    @GetMapping("/me/bid-stats")
    public Result<java.util.List<Map<String, Object>>> getBidStats(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return Result.success(userService.findSellerBidStats(userId));
    }
}