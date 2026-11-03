package com.leviathan.bidpass.config;

import com.leviathan.bidpass.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String method = request.getMethod();
        String path = request.getRequestURI();

        // Public GET endpoints.
        // Seller listings and private ticket photos are excluded - they must go through auth below.
        boolean isPrivatePhoto = method.equals("GET") &&
                path.matches(".*/api/tickets/[^/]+/ticket-photo");
        boolean isMyListings = method.equals("GET") &&
            path.equals("/api/tickets/my");
        boolean isBidStats = method.equals("GET") &&
            path.equals("/api/users/me/bid-stats");
        boolean isMyProfile = method.equals("GET") &&
            path.equals("/api/users/me");
        if (method.equals("GET") && !isPrivatePhoto && !isMyListings && !isBidStats && !isMyProfile && (
            path.startsWith("/api/tickets") ||
            path.startsWith("/api/categories") ||
            path.startsWith("/api/users") ||
            path.startsWith("/api/auctions") ||
            path.startsWith("/api/reviews") ||
            path.startsWith("/api/ai/price-insight")
        )) {
            return true;
        }

        // Public AI ticket extraction endpoint
        if (method.equals("POST") &&
                path.equals("/api/ai/extract-ticket")) {
            return true;
        }

        // ADDED: Public AI customer chat endpoint
        if (method.equals("POST") &&
                path.equals("/api/ai/complaint")) {
            return true;
        }

        // All other endpoints require authentication
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"code\":401,\"message\":\"Unauthorized\",\"data\":null}"
            );
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"code\":401,\"message\":\"Invalid or expired token\",\"data\":null}"
            );
            return false;
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }
}