package com.ai.chat.ChatGPT.Service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String askAI(String userInput) {
        try {

            JSONObject json = new JSONObject();
            json.put("model", "gpt-5-nano");

            JSONArray inputArray = new JSONArray();

            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    "You are a professional assistant. " +
                            "For simple greetings, thanks, or closings, provide a single, polite one-line response. " +
                            "For complex questions or tasks, provide short, clear numbered points. " +
                            "Keep all answers concise and structured.");

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", userInput);

            inputArray.put(system);
            inputArray.put(user);

            json.put("input", inputArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject obj = new JSONObject(response.body());
            JSONArray output = obj.getJSONArray("output");

            for (int i = 0; i < output.length(); i++) {
                JSONObject message = output.getJSONObject(i);
                if (message.has("content")) {
                    JSONArray content = message.getJSONArray("content");
                    for (int j = 0; j < content.length(); j++) {
                        JSONObject textObj = content.getJSONObject(j);
                        if (textObj.has("text")) {
                            return textObj.getString("text");
                        }
                    }
                }
            }

            return "No response";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI";
        }
    }
}