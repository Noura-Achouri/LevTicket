package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Order;
import com.leviathan.bidpass.service.OrderService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public Result<Order> getOrderById(
            @PathVariable Integer id,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        Order order = orderService.findById(id);
        if (order == null) {
            return Result.error(404, "Order not found");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.error(403, "You are not a party to this order");
        }
        return Result.success(order);
    }

    @GetMapping
    public Result<List<Order>> getMyOrders(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "bought") String type) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        List<Order> orders;
        if (type.equals("sold")) {
            orders = orderService.findBySellerId(userId);
        } else {
            orders = orderService.findByBuyerId(userId);
        }
        return Result.success(orders);
    }

    @PostMapping("/from-auction/{auctionId}")
    public Result<Order> createOrderFromAuction(
            @PathVariable Integer auctionId,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        try {
            Order order = orderService.createOrderFromAuction(auctionId);
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(
            @PathVariable Integer id,
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        String status = body.get("status");
        if (status == null) {
            return Result.error(400, "Status is required");
        }

        try {
            orderService.updateStatus(id, userId, status);
            return Result.success("Order status updated");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}