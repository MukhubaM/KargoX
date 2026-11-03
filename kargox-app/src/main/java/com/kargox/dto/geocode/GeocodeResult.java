package com.kargox.dto.geocode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeocodeResult {

    private String displayName;
    private double latitude;
    private double longitude;
}
