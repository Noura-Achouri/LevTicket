package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Ticket;
import com.leviathan.bidpass.service.TicketService;
import com.leviathan.bidpass.service.WatchlistService;
import com.leviathan.bidpass.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping
    public Result<Map<String, Object>> getTickets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "17") int size) {

        Map<String, Object> data = ticketService.findAll(keyword, categoryId, location, tag, sort, page, size);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Ticket> getTicketById(@PathVariable Integer id) {
        Ticket ticket = ticketService.findById(id);
        if (ticket == null) {
            return Result.error(404, "Ticket not found");
        }
        return Result.success(ticket);
    }

    /** Public social proof: how many people have saved this ticket. */
    @GetMapping("/{id}/watchers")
    public Result<Map<String, Object>> getWatcherCount(@PathVariable Integer id) {
        Map<String, Object> data = new HashMap<>();
        data.put("count", watchlistService.countForTicket(id));
        return Result.success(data);
    }

    @PostMapping
    public Result<Map<String, Object>> createTicket(
            HttpServletRequest request,
            @RequestParam Map<String, String> body,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "ticketImages", required = false) MultipartFile[] ticketImages) {

        Integer sellerId = (Integer) request.getAttribute("userId");
        if (sellerId == null) {
            return Result.error(401, "Unauthorized");
    }

    try {
        String eventName = (String) body.get("eventName");
        String eventDate = (String) body.get("eventDate");
        String eventLocation = (String) body.get("eventLocation");
        String seatInfo = (String) body.get("seatInfo");
        String description = (String) body.get("description");
        Integer categoryId = body.get("categoryId") != null ? Integer.parseInt(body.get("categoryId")) : null;
        String tags = (String) body.get("tags");
        
        Object startPriceObj = body.get("startPrice");
             if (startPriceObj == null) {   
            return Result.error(400, "startPrice is required");
        }
        BigDecimal startPrice = new BigDecimal(startPriceObj.toString());

        if (eventName == null || eventDate == null || startPrice == null) {
            return Result.error(400, "eventName, eventDate and startPrice are required");
        }

        int durationHours = 48;
        String durationObj = body.get("auctionDurationHours");
        if (durationObj != null) {
            durationHours = Integer.parseInt(durationObj);
        }

        Map<String, Object> result = ticketService.createTicket(sellerId, categoryId, eventName,
                eventDate, eventLocation, seatInfo, description, startPrice, tags, durationHours);

        Integer ticketId = (Integer) result.get("ticketId");
        saveUploadedImages(ticketId, images, false);
        saveUploadedImages(ticketId, ticketImages, true);

        return Result.success(result);

    } catch (Exception e) {
        return Result.error(400, "Invalid request: " + e.getMessage());
    }
}

    @GetMapping("/my")
    public Result<List<Ticket>> getMyTickets(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {

        Integer sellerId = (Integer) request.getAttribute("userId");
        if (sellerId == null) {
            return Result.error(401, "Unauthorized");
        }

        List<Ticket> tickets = ticketService.findBySellerId(sellerId, status);
        return Result.success(tickets);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTicket(@PathVariable Integer id, HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("userId");
        if (sellerId == null) return Result.error(401, "Unauthorized");
        if (!ticketService.deleteTicket(id, sellerId)) {
            return Result.error(404, "Active ticket not found or it does not belong to you");
        }
        return Result.success("Ticket removed");
    }

    @GetMapping("/{id}/ticket-photo")
    public Result<Map<String, Object>> getTicketPhoto(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }
        if (!ticketService.canViewTicketPhoto(id, userId)) {
            return Result.error(403, "Ticket photo is private until the seller closes the auction");
        }
        List<String> urls = ticketService.findPrivateImages(id);
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrls", urls);
        return Result.success(data);
    }

    private void saveUploadedImages(Integer ticketId, MultipartFile[] files, boolean isPrivate) {
        if (files == null) return;
        try {
            Path uploadDirectory = Paths.get("uploads", "tickets");
            Files.createDirectories(uploadDirectory);
            for (MultipartFile image : files) {
                if (image == null || image.isEmpty()) continue;
                String originalName = image.getOriginalFilename();
                String extension = originalName != null && originalName.contains(".")
                        ? originalName.substring(originalName.lastIndexOf('.')) : ".bin";
                Path destination = uploadDirectory.resolve(UUID.randomUUID() + extension);
                image.transferTo(destination);
                String imageUrl = "/uploads/tickets/" + destination.getFileName();
                if (isPrivate) {
                    ticketService.addPrivateImage(ticketId, imageUrl);
                } else {
                    ticketService.addImage(ticketId, imageUrl);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }
}