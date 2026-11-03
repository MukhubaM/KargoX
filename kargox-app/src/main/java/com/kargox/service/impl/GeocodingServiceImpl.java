package com.kargox.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kargox.dto.geocode.GeocodeResult;
import com.kargox.dto.geocode.ReverseGeocodeResult;
import com.kargox.exception.BadRequestException;
import com.kargox.service.GeocodingService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "KargoX-Logistics-App/1.0 (dev/demo; contact admin@kargox.com)";
    private static final Duration MIN_INTERVAL = Duration.ofMillis(1100);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, List<GeocodeResult>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, ReverseGeocodeResult> reverseCache = new ConcurrentHashMap<>();

    private final Object throttleLock = new Object();
    private Instant lastRequestAt = Instant.EPOCH;

    @Override
    public List<GeocodeResult> search(String query) {

        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query must not be empty");
        }
        String cacheKey = query.trim().toLowerCase();
        List<GeocodeResult> cached = searchCache.get(cacheKey);
        if (cached != null) {

            return cached;
        }

        throttle();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URI uri = URI.create(BASE_URL + "/search?q=" + encoded + "&format=json&limit=5&addressdetails=0");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "en")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<GeocodeResult> results = parseSearchResults(response.body());
            searchCache.put(cacheKey, results);

            return results;
        } catch (Exception e) {
            log.warn("Geocoding search failed for '{}': {}", query, e.getMessage());
            throw new BadRequestException("Could not look up that address right now. Please try again.");
        }
    }

    @Override
    public ReverseGeocodeResult reverse(double latitude, double longitude) {

        String cacheKey = round(latitude) + "," + round(longitude);
        ReverseGeocodeResult cached = reverseCache.get(cacheKey);

        if (cached != null) {

            return cached;
        }

        throttle();
        try {
            URI uri = URI.create(BASE_URL + "/reverse?lat=" + latitude + "&lon=" + longitude + "&format=json");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "en")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            String displayName = root.has("display_name")
                    ? root.get("display_name").asText()
                    : String.format("%.5f, %.5f", latitude, longitude);

            ReverseGeocodeResult result = new ReverseGeocodeResult(displayName);
            reverseCache.put(cacheKey, result);

            return result;
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for {},{}: {}", latitude, longitude, e.getMessage());

            return new ReverseGeocodeResult(String.format("%.5f, %.5f", latitude, longitude));
        }
    }

    private List<GeocodeResult> parseSearchResults(String body) throws Exception {

        List<GeocodeResult> results = new ArrayList<>();
        JsonNode array = objectMapper.readTree(body);

        if (array.isArray()) {
            for (JsonNode node : array) {
                double lat = node.get("lat").asDouble();
                double lon = node.get("lon").asDouble();
                String name = node.has("display_name") ? node.get("display_name").asText() : "";
                results.add(new GeocodeResult(name, lat, lon));
            }
        }

        return results;
    }

    private void throttle() {

        synchronized (throttleLock) {
            Duration sinceLast = Duration.between(lastRequestAt, Instant.now());
            if (sinceLast.compareTo(MIN_INTERVAL) < 0) {
                try {
                    Thread.sleep(MIN_INTERVAL.minus(sinceLast).toMillis());
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestAt = Instant.now();
        }
    }

    private double round(double value) {

        return Math.round(value * 10000.0) / 10000.0;
    }
}
