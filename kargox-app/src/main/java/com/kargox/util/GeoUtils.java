package com.kargox.util;

// Haversine-based geo distance helper, used by the driver matching engine and fare calculation service
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // Rough ETA assumption on average urban delivery-vehicle speed(80 km/h)
    public static int estimatedMinutes(double distanceKm) {

        double avgSpeedKmh = 80.0;

        return (int) Math.max(1, Math.round((distanceKm / avgSpeedKmh) * 60));
    }
}
