package com.kargox.dto.geocode;

import lombok.AllArgsConstructor;
import lombok.Getter;

// The address found for a given lat/lng (used for 'drag pin' and 'use my location').
@Getter
@AllArgsConstructor
public class ReverseGeocodeResult {

    private String displayName;
}
