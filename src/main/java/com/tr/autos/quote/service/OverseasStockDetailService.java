package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.dto.StockDetailDto;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.market.kis.KisOverseasPriceClient;
import com.tr.autos.market.kis.dto.KisOverseasPriceDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverseasStockDetailService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KisOverseasPriceClient overseasPriceClient;

    @Transactional(readOnly = true)
    public StockDetailDto read(Symbol symbol) {
        String exchangeCode = determineExchangeCode(symbol);
        KisOverseasPriceDetailResponse response = overseasPriceClient.fetchPriceDetail(exchangeCode, symbol.getTicker());

        validateResponse(symbol, response);

        KisOverseasPriceDetailResponse.Output output = response.output();
        if (output == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KIS 해외 시세 응답이 비어 있습니다.");
        }

        long price = parseLongOrZero(output.convertedTodayPrice(), output.last());
        long prevDiff = parseLongOrZero(output.convertedTodayDiff(), output.convertedPrevDiff());
        Double changeRate = parseDoubleFirst(output.convertedTodayRate(), output.convertedPrevRate());
        int changeSign = determineChangeSign(prevDiff, changeRate, output.convertedTodaySign());

        Long open = parseLongNullable(output.open());
        Long high = parseLongNullable(output.high());
        Long low = parseLongNullable(output.low());
        Long upperLimit = parseLongNullable(output.upperLimit());
        Long lowerLimit = parseLongNullable(output.lowerLimit());
        Long refPrice = parseLongNullable(output.base());
        Long volume = parseLongNullable(output.todayVolume());
        Long amount = parseLongNullable(output.todayAmount());
        Double volumeRateVsPrev = calculateVolumeRate(output.todayVolume(), output.previousVolume());
        Long sharesOutstanding = parseLongNullable(output.sharesOutstanding());
        Long marketCap = parseLongNullable(output.marketCap());
        Double per = parseDouble(output.per());
        Double pbr = parseDouble(output.pbr());
        Double eps = parseDouble(output.eps());
        Double bps = parseDouble(output.bps());
        Long high52w = parseLongNullable(output.high52Week());
        LocalDate high52wDate = parseLocalDate(output.high52WeekDate());
        Long low52w = parseLongNullable(output.low52Week());
        LocalDate low52wDate = parseLocalDate(output.low52WeekDate());

        return new StockDetailDto(
                symbol.getId(),
                symbol.getTicker(),
                symbol.getMarket(),
                symbol.getName(),
                price,
                prevDiff,
                changeRate,
                changeSign,
                open,
                high,
                low,
                upperLimit,
                lowerLimit,
                refPrice,
                volume,
                amount,
                volumeRateVsPrev,
                null,
                null,
                sharesOutstanding,
                marketCap,
                per,
                pbr,
                eps,
                bps,
                null,
                high52w,
                high52wDate,
                null,
                low52w,
                low52wDate,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(KST)
        );
    }

    private void validateResponse(Symbol symbol, KisOverseasPriceDetailResponse response) {
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KIS 해외 시세 응답이 비어 있습니다.");
        }
        if (!"0".equals(response.rtCd())) {
            String message = response.msg() != null ? response.msg() : "KIS API 오류";
            log.warn("[KIS] 해외 시세 조회 실패 symbol={} rt_cd={} msg={}", symbol.getTicker(), response.rtCd(), message);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
        }
    }

    private String determineExchangeCode(Symbol symbol) {
        if ("US".equalsIgnoreCase(symbol.getMarket())) {
            return "NAS";
        }
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "지원하지 않는 해외 시장입니다: " + symbol.getMarket());
    }

    private long parseLongOrZero(String... candidates) {
        for (String candidate : candidates) {
            Long value = parseLongNullable(candidate);
            if (value != null) {
                return value;
            }
        }
        return 0L;
    }

    private Long parseLongNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String sanitized = value.replace(",", "").trim();
            if (sanitized.isEmpty()) {
                return null;
            }
            BigDecimal decimal = new BigDecimal(sanitized);
            return decimal.setScale(0, RoundingMode.HALF_UP).longValue();
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }

    private Double parseDoubleFirst(String... candidates) {
        for (String candidate : candidates) {
            Double value = parseDouble(candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String sanitized = value.replace(",", "").replace("%", "").trim();
            if (sanitized.isEmpty()) {
                return null;
            }
            return Double.parseDouble(sanitized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double calculateVolumeRate(String todayVolumeStr, String previousVolumeStr) {
        Long todayVolume = parseLongNullable(todayVolumeStr);
        Long previousVolume = parseLongNullable(previousVolumeStr);
        if (todayVolume == null || previousVolume == null || previousVolume == 0) {
            return null;
        }
        double diff = todayVolume - previousVolume;
        return (diff / previousVolume.doubleValue()) * 100.0;
    }

    private int determineChangeSign(long prevDiff, Double changeRate, String signFlag) {
        if (prevDiff > 0) {
            return 1;
        }
        if (prevDiff < 0) {
            return 2;
        }
        if (changeRate != null) {
            if (changeRate > 0) {
                return 1;
            }
            if (changeRate < 0) {
                return 2;
            }
        }
        if (signFlag != null) {
            return switch (signFlag.trim()) {
                case "1", "+", "UP" -> 1;
                case "2", "-", "DN", "DOWN" -> 2;
                default -> 0;
            };
        }
        return 0;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String sanitized = value.trim();
        try {
            if (sanitized.length() == 8) {
                return LocalDate.parse(sanitized, DATE_YYYYMMDD);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
