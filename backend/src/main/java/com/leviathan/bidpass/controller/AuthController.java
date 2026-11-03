package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.User;
import com.leviathan.bidpass.service.UserService;
import com.leviathan.bidpass.util.JwtUtil;
import com.leviathan.bidpass.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String phone = body.get("phone");

        if (username == null || email == null || password == null) {
            return Result.error(400, "Username, email and password are required");
        }

        try {
            User user = userService.register(username, email, password, phone);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(409, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return Result.error(400, "Email and password are required");
        }

        try {
            User user = userService.login(email, password);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("verificationStatus", user.getVerificationStatus());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }
}