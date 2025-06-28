package com.universitycrud.controller;

import com.universitycrud.service.GeminiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/summarizes")
public class SummarizerController {

    private final GeminiService geminiService;

    public SummarizerController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping
    public String showForm() {
        return "summarize/form";
    }

    @PostMapping("/chat")
    public String summarizeText(@RequestParam("inputText") String inputText, Model model) {
        String prompt = "\n"+inputText;

        String summary = geminiService.getGeminiResponseText(prompt);
        model.addAttribute("original", inputText);
        model.addAttribute("summary", summary);

        return "summarize/result";
    }
}
