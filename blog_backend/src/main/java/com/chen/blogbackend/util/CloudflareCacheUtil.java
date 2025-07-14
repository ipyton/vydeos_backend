package com.chen.blogbackend.util;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cloudflare缓存清除服务
 */
public class CloudflareCacheUtil {

    private static final String apiToken = System.getenv("CLOUDFLARE_API_KEY");
    private static final String zoneId = "a7331783bde6f52242855bb1d1ad910b";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();;
    private static final ObjectMapper objectMapper = new ObjectMapper();;

    private static final String CLOUDFLARE_API_BASE_URL = "https://api.cloudflare.com/client/v4";


    /**
     * 清除所有缓存
     */
    public static PurgeResponse purgeAll() throws IOException, InterruptedException {
        String url = String.format("%s/zones/%s/purge_cache", CLOUDFLARE_API_BASE_URL, zoneId);
        PurgeAllRequest request = new PurgeAllRequest(true);
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), PurgeResponse.class);
    }

    /**
     * 根据URL清除指定缓存
     */
    public static PurgeResponse purgeByUrls(List<String> urls) throws IOException, InterruptedException {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URLs列表不能为空");
        }

        if (urls.size() > 30) {
            throw new IllegalArgumentException("单次最多只能清除30个URL");
        }

        String url = String.format("%s/zones/%s/purge_cache", CLOUDFLARE_API_BASE_URL, zoneId);

        PurgeByUrlsRequest request = new PurgeByUrlsRequest(urls);
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), PurgeResponse.class);
    }

    /**
     * 根据标签清除缓存
     */
    public PurgeResponse purgeByTags(List<String> tags) throws IOException, InterruptedException {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("标签列表不能为空");
        }

        String url = String.format("%s/zones/%s/purge_cache", CLOUDFLARE_API_BASE_URL, zoneId);

        PurgeByTagsRequest request = new PurgeByTagsRequest(tags);
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), PurgeResponse.class);
    }

    /**
     * 异步清除所有缓存
     */
    public CompletableFuture<PurgeResponse> purgeAllAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return purgeAll();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 异步根据URL清除缓存
     */
    public CompletableFuture<PurgeResponse> purgeByUrlsAsync(List<String> urls) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return purgeByUrls(urls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 请求数据类
    static class PurgeAllRequest {
        @JsonProperty("purge_everything")
        private boolean purgeEverything;

        public PurgeAllRequest(boolean purgeEverything) {
            this.purgeEverything = purgeEverything;
        }

        public boolean isPurgeEverything() {
            return purgeEverything;
        }
    }

    static class PurgeByUrlsRequest {
        private List<String> files;

        public PurgeByUrlsRequest(List<String> files) {
            this.files = files;
        }

        public List<String> getFiles() {
            return files;
        }
    }

    static class PurgeByTagsRequest {
        private List<String> tags;

        public PurgeByTagsRequest(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    // 响应数据类
    public static class PurgeResponse {
        private boolean success;
        private List<String> errors;
        private List<String> messages;
        private PurgeResult result;

        // Getters and setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getMessages() {
            return messages;
        }

        public void setMessages(List<String> messages) {
            this.messages = messages;
        }

        public PurgeResult getResult() {
            return result;
        }

        public void setResult(PurgeResult result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return String.format("PurgeResponse{success=%s, errors=%s, messages=%s, result=%s}",
                    success, errors, messages, result);
        }
    }

    public static class PurgeResult {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return String.format("PurgeResult{id='%s'}", id);
        }
    }
}