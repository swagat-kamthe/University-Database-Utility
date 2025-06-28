package com.universitycrud.model;

import java.util.List;
import java.util.Map;

public class PromptRequest {
    private List<Map<String, String>> contents;

    public PromptRequest(String prompt) {
        this.contents = List.of(Map.of("role", "user", "parts", prompt));
    }

    public List<Map<String, String>> getContents() {
        return contents;
    }

    public void setContents(List<Map<String, String>> contents) {
        this.contents = contents;
    }
}
