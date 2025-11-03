package com.behl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class ChatbotController {

    private final ChatbotService chatbotService;

    ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
        var answer = chatbotService.chat(chatRequest.question);
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    record ChatRequest(String question) {
    }

    record ChatResponse(String answer) {
    }

}