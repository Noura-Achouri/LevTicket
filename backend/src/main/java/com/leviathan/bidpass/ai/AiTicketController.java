package com.leviathan.bidpass.ai;

import com.leviathan.bidpass.ai.dto.TicketExtractDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTicketController {
    private final TicketAiService ticketAiService;

    @PostMapping("/extract-ticket")
    public Mono<ResponseEntity<TicketExtractDTO>> extractTicket(
            @RequestParam("ticketImage") MultipartFile image) throws Exception {

        return ticketAiService.extractTicketData(image)
                .map(dto -> ResponseEntity.ok(dto));
    }

    // ADDED: AI customer chat
    @PostMapping("/complaint")
    public Mono<ResponseEntity<Map<String, String>>> complaint(
            @RequestBody Map<String, Object> request) {

        String message = String.valueOf(request.getOrDefault("message", ""));
        String image = request.get("image") == null ? null : request.get("image").toString();
        String userName = request.get("userName") == null ? "" : request.get("userName").toString();

        List<Map<String, String>> history = new ArrayList<>();
        Object rawHistory = request.get("history");
        if (rawHistory instanceof List<?>) {
            for (Object item : (List<?>) rawHistory) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, String> turn = new java.util.HashMap<>();
                    Object role = map.get("role");
                    Object content = map.get("content");
                    if (role != null) turn.put("role", role.toString());
                    if (content != null) turn.put("content", content.toString());
                    history.add(turn);
                }
            }
        }

        return ticketAiService.chat(message, history, image, userName)
                .map(reply -> ResponseEntity.ok(
                        Map.of("reply", reply)
                ));
    }
}