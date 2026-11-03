package com.kargox.service;

import com.kargox.dto.geocode.GeocodeResult;
import com.kargox.dto.geocode.ReverseGeocodeResult;

import java.util.List;

public interface GeocodingService {

    // Forward geocode (turns a free-text address into candidate matches with coordinates)
    List<GeocodeResult> search(String query);

    // Reverse geocode (turns coordinates into a human-readable address)
    ReverseGeocodeResult reverse(double latitude, double longitude);
}
