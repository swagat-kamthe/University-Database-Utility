package com.universitycrud.controller;

import org.springframework.web.bind.annotation.*;

import com.universitycrud.service.GeminiService;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public String chatWithGemini(@RequestBody InputRequest inputRequest) {
        return geminiService.getGeminiResponseText(inputRequest.getText());
    }

    public static class InputRequest {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}