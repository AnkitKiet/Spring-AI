package com.example.springboot.helloworld.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void saveNote(String noteText) {
        // 1. Generate Embedding from Ollama
        Map<String, Object> embeddingReq = new HashMap<>();
        embeddingReq.put("model", "nomic-embed-text");
        embeddingReq.put("prompt", noteText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(embeddingReq, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:11434/api/embeddings", request, Map.class);

        List<Double> embedding = (List<Double>) response.getBody().get("embedding");
        List<Float> vector = embedding.stream().map(Double::floatValue).toList();

        // 2. Generate UUID
        String uuid = UUID.randomUUID().toString();

        // 3. Store in Qdrant
        Map<String, Object> payload = Map.of("text", noteText);
        Map<String, Object> point = Map.of(
                "id", uuid,
                "vector", vector,
                "payload", payload);

        Map<String, Object> insertBody = Map.of("points", List.of(point));

        HttpEntity<Map<String, Object>> insertRequest = new HttpEntity<>(insertBody, headers);
        restTemplate.put("http://localhost:6333/collections/notes/points", insertRequest);
    }

    public List<String> searchNotes(String query) {
        // 1. Get embedding from Ollama
        Map<String, Object> embeddingReq = Map.of(
                "model", "nomic-embed-text",
                "prompt", query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> embedRequest = new HttpEntity<>(embeddingReq, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:11434/api/embeddings",
                embedRequest,
                Map.class);

        List<Double> embedding = (List<Double>) response.getBody().get("embedding");
        List<Float> vector = embedding.stream().map(Double::floatValue).toList();
        // 2. Search in Qdrant
        Map<String, Object> searchRequest = Map.of(
                "vector", vector,
                "top",15,
                "with_payload", true);

        HttpEntity<Map<String, Object>> searchHttp = new HttpEntity<>(searchRequest, headers);
        ResponseEntity<Map> qdrantResponse = restTemplate.postForEntity(
                "http://localhost:6333/collections/notes/points/search",
                searchHttp,
                Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) qdrantResponse.getBody().get("result");
        System.out.println("Results size: " + results.size());
        List<String> notes = new ArrayList<>();
        for (Map<String, Object> result : results) {
            Map<String, Object> payload = (Map<String, Object>) result.get("payload");
            if (payload != null && payload.containsKey("text")) {
                notes.add(payload.get("text").toString() + "\n");
            }
        }

        return notes;
    }

    public void saveBulkNotes(List<String> notes) {
        List<Map<String, Object>> points = new ArrayList<>();

        for (String noteText : notes) {
            // 1. Generate embedding from Ollama
            Map<String, Object> embeddingReq = Map.of(
                    "model", "nomic-embed-text",
                    "prompt", noteText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(embeddingReq, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:11434/api/embeddings", request, Map.class);

            List<Double> embedding = (List<Double>) response.getBody().get("embedding");
            List<Float> vector = embedding.stream().map(Double::floatValue).toList();

            String uuid = UUID.randomUUID().toString();
            Map<String, Object> payload = Map.of("text", noteText);

            Map<String, Object> point = Map.of(
                    "id", uuid,
                    "vector", vector,
                    "payload", payload);

            points.add(point);
        }

        // 2. Send all points in one batch to Qdrant
        Map<String, Object> insertBody = Map.of("points", points);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> insertRequest = new HttpEntity<>(insertBody, headers);

        restTemplate.put("http://localhost:6333/collections/notes/points", insertRequest);
    }

}
