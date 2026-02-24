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

    @GetMapping("/about")
    public String about() {
        return "about";
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

    @GetMapping("/api/new-chat")
    @ResponseBody
    public String newChat() {
        chatService.startNewSession();
        return "New chat started";
    }

    @GetMapping("/api/clear-chat")
    @ResponseBody
    public String clearChat() {
        chatService.clearCurrentSession();
        return "Chat cleared";
    }

    @DeleteMapping("/api/history/{id}")
    @ResponseBody
    public String deleteHistory(@PathVariable String id) {
        chatService.deleteSession(id);
        return "Deleted";
    }

    @DeleteMapping("/api/history/clear-all")
    @ResponseBody
    public String clearAllHistory() {
        chatService.clearAllSessions();
        return "All history cleared";
    }

    @PostMapping("/api/history/{id}/rename")
    @ResponseBody
    public String renameHistory(@PathVariable String id, @RequestParam String title) {
        chatService.renameSession(id, title);
        return "Renamed";
    }

    @GetMapping("/api/history")
    @ResponseBody
    public java.util.List<com.ai.chat.ChatGPT.Service.ChatService.ChatSession> getAllSessions() {
        return chatService.getAllSessions();
    }

    @GetMapping("/api/history/{id}")
    @ResponseBody
    public com.ai.chat.ChatGPT.Service.ChatService.ChatSession getSession(@PathVariable String id) {
        return chatService.getSession(id);
    }
}