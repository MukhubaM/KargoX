package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.geocode.GeocodeResult;
import com.kargox.dto.geocode.ReverseGeocodeResult;
import com.kargox.service.GeocodingService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geocode")
@RequiredArgsConstructor
@Tag(name = "Geocoding", description = "Address search and reverse lookup (OpenStreetMap/Nominatim)")
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GeocodeResult>>> search(@RequestParam String q) {

        return ResponseEntity.ok(ApiResponse.ok(geocodingService.search(q)));
    }

    @GetMapping("/reverse")
    public ResponseEntity<ApiResponse<ReverseGeocodeResult>> reverse(@RequestParam
                                                                     double lat,
                                                                     @RequestParam
                                                                     double lon) {

        return ResponseEntity.ok(ApiResponse.ok(geocodingService.reverse(lat, lon)));
    }
}
