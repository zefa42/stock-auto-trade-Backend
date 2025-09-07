package com.tr.autos.error;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private final String code;            // 예: BAD_REQUEST, UNAUTHORIZED
    private final String message;         // 사람 읽을 메시지
    private final String path;            // 요청 URI
    private final Instant timestamp;      // 발생 시각
    private final Map<String, ?> details; // 필드 에러 등 부가정보(없으면 null)

}
