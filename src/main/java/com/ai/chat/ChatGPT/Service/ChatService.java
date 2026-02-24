package com.ai.chat.ChatGPT.Service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final List<ChatSession> allSessions = new ArrayList<>();
    private ChatSession currentSession;

    public static class ChatMessage {
        public String role;
        public String content;
        public String timestamp;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        }
    }

    public static class ChatSession {
        public String id;
        public String title;
        public List<ChatMessage> messages = new ArrayList<>();
        public String date;

        public ChatSession(String firstQuestion) {
            this.id = UUID.randomUUID().toString();
            this.title = firstQuestion.length() > 30 ? firstQuestion.substring(0, 27) + "..." : firstQuestion;
            this.date = new SimpleDateFormat("MMM dd").format(new Date());
        }
    }

    public ChatService() {
        startNewSession();
    }

    public void startNewSession() {
        currentSession = null; 
    }

    public void clearAllSessions() {
        allSessions.clear();
        currentSession = null;
    }

    public void clearCurrentSession() {
        if (currentSession != null) {
            currentSession.messages.clear();
        }
    }

    public void deleteSession(String id) {
        allSessions.removeIf(s -> s.id.equals(id));
        if (currentSession != null && currentSession.id.equals(id)) {
            currentSession = null;
        }
    }

    public void renameSession(String id, String newTitle) {
        allSessions.stream()
            .filter(s -> s.id.equals(id))
            .findFirst()
            .ifPresent(s -> s.title = newTitle);
    }

    public List<ChatSession> getAllSessions() {
        return allSessions;
    }

    public ChatSession getSession(String id) {
        ChatSession session = allSessions.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
        if (session != null) {
            currentSession = session; 
        }
        return session;
    }

    public String askAI(String userInput) {
        if (currentSession == null) {
            currentSession = new ChatSession(userInput);
            allSessions.add(currentSession);
        }

        currentSession.messages.add(new ChatMessage("user", userInput));

        String aiAnswer = "No response";
        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-5-nano");
            JSONArray inputArray = new JSONArray();

            // System prompt
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "You are a professional assistant. Answer concisely.");
            inputArray.put(system);

            // ONLY send the current user message to ensure API compatibility
            JSONObject currentUserMsg = new JSONObject();
            currentUserMsg.put("role", "user");
            currentUserMsg.put("content", userInput);
            inputArray.put(currentUserMsg);

            json.put("input", inputArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject obj = new JSONObject(response.body());
                if (obj.has("output")) {
                    JSONArray output = obj.getJSONArray("output");
                    for (int i = 0; i < output.length(); i++) {
                        JSONObject message = output.getJSONObject(i);
                        if (message.has("content")) {
                            JSONArray content = message.getJSONArray("content");
                            for (int j = 0; j < content.length(); j++) {
                                JSONObject textObj = content.getJSONObject(j);
                                if (textObj.has("text")) {
                                    aiAnswer = textObj.getString("text");
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                aiAnswer = "Server Error: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            aiAnswer = "Error calling AI";
        }

        currentSession.messages.add(new ChatMessage("ai", aiAnswer));
        return aiAnswer;
    }
}