package com.yunlan.service;

import java.util.Map;

public interface AmapService {
    /**
     * Reverse geocode: coordinates → province/city/district/street
     * @param lat latitude
     * @param lng longitude
     * @return map with province, city, district, street, streetNumber, or null if unavailable
     */
    Map<String, Object> reverseGeocode(String lat, String lng);
}
