package com.tr.autos.utils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * KIS API 응답 데이터를 안전하게 변환하는 유틸리티 클래스
 */
public final class DataConversionUtils {
    
    private DataConversionUtils() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
    
    /**
     * Object를 Long으로 안전하게 변환
     * @param value 변환할 값
     * @return 변환된 Long 값, 변환 실패 시 null
     */
    public static Long toLong(Object value) {
        if (value == null) return null;
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) return null;
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Object를 Double로 안전하게 변환 (퍼센트 기호 제거)
     * @param value 변환할 값
     * @return 변환된 Double 값, 변환 실패 시 null
     */
    public static Double toDouble(Object value) {
        if (value == null) return null;
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) return null;
            // 퍼센트 기호 제거
            str = str.replace("%", "");
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Object를 Integer로 안전하게 변환
     * @param value 변환할 값
     * @return 변환된 Integer 값, 변환 실패 시 null
     */
    public static Integer toInt(Object value) {
        if (value == null) return null;
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) return null;
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Y/N 문자열을 Boolean으로 안전하게 변환
     * @param value 변환할 값 (Y/N)
     * @return 변환된 Boolean 값, 변환 실패 시 null
     */
    public static Boolean toBoolYN(Object value) {
        if (value == null) return null;
        String str = value.toString().trim().toUpperCase();
        if (str.isEmpty()) return null;
        return "Y".equals(str);
    }
    
    /**
     * yyyyMMdd 형식의 문자열을 java.sql.Date로 변환
     * @param value 변환할 값 (yyyyMMdd 형식)
     * @return 변환된 Date 값, 변환 실패 시 null
     */
    public static Date toSqlDateYYYYMMDD(Object value) {
        if (value == null) return null;
        try {
            String str = value.toString().trim();
            if (str.isEmpty() || str.length() != 8) return null;
            
            LocalDate localDate = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * 시가총액을 백만원 단위에서 원 단위로 변환
     * @param value 변환할 값
     * @param unit 단위 ("백만원" 또는 "원")
     * @return 변환된 시가총액 값 (원 단위), 변환 실패 시 null
     */
    public static Long normalizeMarketCap(Object value, String unit) {
        Long marketCap = toLong(value);
        if (marketCap == null) return null;
        
        if ("백만원".equals(unit) || "million".equalsIgnoreCase(unit)) {
            return marketCap * 1_000_000L;
        }
        return marketCap; // 이미 원 단위인 경우
    }
    
    /**
     * Object를 String으로 안전하게 변환 (null 방지)
     * @param value 변환할 값
     * @return 변환된 String 값, null이면 null 반환
     */
    public static String toString(Object value) {
        if (value == null) return null;
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
    }
}