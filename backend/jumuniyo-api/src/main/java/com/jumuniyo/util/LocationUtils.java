package com.jumuniyo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 위치 기반 검색을 위한 유틸리티 클래스
 */
public class LocationUtils {

    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (킬로미터)

    /**
     * 두 지점 간의 거리를 계산합니다 (Haversine 공식 사용)
     * 
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 두 지점 간의 거리 (킬로미터)
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE; // 위치 정보가 없으면 최대값 반환
        }

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 두 지점 간의 거리를 계산합니다 (double 타입)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(
            BigDecimal.valueOf(lat1), 
            BigDecimal.valueOf(lon1), 
            BigDecimal.valueOf(lat2), 
            BigDecimal.valueOf(lon2)
        );
    }

    /**
     * 지정된 반경 내에 있는지 확인합니다
     * 
     * @param userLat 사용자 위도
     * @param userLon 사용자 경도
     * @param storeLat 가게 위도
     * @param storeLon 가게 경도
     * @param radiusKm 반경 (킬로미터)
     * @return 반경 내에 있으면 true
     */
    public static boolean isWithinRadius(BigDecimal userLat, BigDecimal userLon, 
                                       BigDecimal storeLat, BigDecimal storeLon, 
                                       double radiusKm) {
        double distance = calculateDistance(userLat, userLon, storeLat, storeLon);
        return distance <= radiusKm;
    }

    /**
     * 거리를 반올림하여 BigDecimal로 반환합니다
     */
    public static BigDecimal calculateDistanceAsBigDecimal(BigDecimal lat1, BigDecimal lon1, 
                                                         BigDecimal lat2, BigDecimal lon2) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 위도/경도 유효성 검사
     */
    public static boolean isValidLatitude(BigDecimal latitude) {
        if (latitude == null) return false;
        double lat = latitude.doubleValue();
        return lat >= -90.0 && lat <= 90.0;
    }

    public static boolean isValidLongitude(BigDecimal longitude) {
        if (longitude == null) return false;
        double lon = longitude.doubleValue();
        return lon >= -180.0 && lon <= 180.0;
    }

    public static boolean isValidCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }

    /**
     * 기본 검색 반경 (킬로미터)
     */
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;

    /**
     * 최대 검색 반경 (킬로미터)
     */
    public static final double MAX_SEARCH_RADIUS_KM = 50.0;
} 