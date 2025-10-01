package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.dto.StockChartDto;
import com.tr.autos.domain.quote.dto.StockChartPointDto;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.market.kis.KisOverseasDailyPriceClient;
import com.tr.autos.market.kis.dto.KisOverseasDailyPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverseasStockChartService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter RESPONSE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int MAX_ITERATIONS = 16;

    private final KisOverseasDailyPriceClient dailyPriceClient;

    public StockChartDto getDailyChart(Symbol symbol,
                                       LocalDate requestedFrom,
                                       LocalDate requestedTo,
                                       String periodCode,
                                       boolean useAdjustedPrice) {
        LocalDate to = requestedTo != null ? requestedTo : LocalDate.now(KST);
        LocalDate from = requestedFrom != null ? requestedFrom : to.minusMonths(3);

        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from 날짜가 to 날짜보다 늦을 수 없습니다.");
        }

        String exchangeCode = determineExchangeCode(symbol);
        String gubn = mapPeriod(periodCode);
        String modp = useAdjustedPrice ? "1" : "0";

        TreeMap<LocalDate, StockChartPointDto> pointsByDate = new TreeMap<>();

        String nextKey = null;
        LocalDate cursor = to;
        int iterations = 0;

        while (!cursor.isBefore(from) && iterations++ < MAX_ITERATIONS) {
            KisOverseasDailyPriceResponse response = dailyPriceClient.fetchDailyPrice(
                    exchangeCode,
                    symbol.getTicker(),
                    gubn,
                    cursor,
                    modp,
                    nextKey
            );

            validateResponse(symbol, response);

            if (response.output2() == null || response.output2().isEmpty()) {
                break;
            }

            response.output2().forEach(entry -> {
                LocalDate date = parseDate(entry.businessDate());
                if (date == null || date.isBefore(from) || date.isAfter(to)) {
                    return;
                }
                StockChartPointDto point = new StockChartPointDto(
                        date,
                        parseLong(entry.closePrice()),
                        parseLong(entry.openPrice()),
                        parseLong(entry.highPrice()),
                        parseLong(entry.lowPrice()),
                        parseLong(entry.accumulatedVolume()),
                        parseLong(entry.accumulatedTransactionAmount())
                );
                pointsByDate.put(date, point);
            });

            LocalDate earliest = response.output2().stream()
                    .map(entry -> parseDate(entry.businessDate()))
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            boolean reachedLowerBound = earliest == null || !earliest.isAfter(from);
            String next = response.output1() != null ? response.output1().keyBuffer() : null;

            if (next != null && !next.isBlank() && !reachedLowerBound) {
                nextKey = next;
                continue;
            }

            nextKey = null;
            if (reachedLowerBound) {
                break;
            }

            cursor = earliest.minusDays(1);
        }

        return new StockChartDto(
                symbol.getId(),
                symbol.getTicker(),
                symbol.getMarket(),
                from,
                to,
                (periodCode == null || periodCode.isBlank()) ? "D" : periodCode.toUpperCase(),
                new ArrayList<>(pointsByDate.values())
        );
    }

    private void validateResponse(Symbol symbol, KisOverseasDailyPriceResponse response) {
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KIS 해외 차트 응답이 비어 있습니다.");
        }
        if (!"0".equals(response.rtCd())) {
            String message = response.msg() != null ? response.msg() : "KIS API 오류";
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

    private String mapPeriod(String periodCode) {
        if (periodCode == null) {
            return "0";
        }
        return switch (periodCode.toUpperCase()) {
            case "W" -> "1";
            case "M" -> "2";
            default -> "0";
        };
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), RESPONSE_DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String value) {
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
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

