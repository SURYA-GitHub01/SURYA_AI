package com.ai.chat.ChatGPT.Controller;

import com.ai.chat.ChatGPT.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, Model model) {

        String answer = chatService.askAI(question);

        model.addAttribute("question", question);
        model.addAttribute("answer", answer);

        return "index";
    }

    @PostMapping("/api/ask")
    @ResponseBody
    public String askApi(@RequestParam String question) {
        return chatService.askAI(question);
    }
}