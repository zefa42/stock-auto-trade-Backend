package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.dto.StockChartDto;
import com.tr.autos.domain.quote.dto.StockChartPointDto;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.market.kis.KisMarketDataClient;
import com.tr.autos.market.kis.dto.KisDailyChartPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockChartService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter RESPONSE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final int MAX_RECORDS_PER_REQUEST = 100;

    private final SymbolRepository symbolRepository;
    private final KisMarketDataClient marketDataClient;

    @Transactional(readOnly = true)
    public StockChartDto getDailyChart(Long symbolId,
                                       LocalDate requestedFrom,
                                       LocalDate requestedTo,
                                       String periodCode,
                                       boolean useAdjustedPrice) {
        Symbol symbol = symbolRepository.findById(symbolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "symbol not found"));

        if (!"KRX".equalsIgnoreCase(symbol.getMarket())) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "국내주식(KRX) 차트만 지원합니다.");
        }

        LocalDate to = requestedTo != null ? requestedTo : LocalDate.now(KST);
        LocalDate from = requestedFrom != null ? requestedFrom : to.minusMonths(3);

        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from 날짜가 to 날짜보다 늦을 수 없습니다.");
        }

        String period = (periodCode == null || periodCode.isBlank()) ? "D" : periodCode.toUpperCase();
        String marketDivCode = "J"; // KRX 전체
        String adjFlag = useAdjustedPrice ? "0" : "1"; // 0:수정주가, 1:원주가

        List<KisDailyChartPriceResponse.Output2> rawOutputs;
        try {
            rawOutputs = fetchChartOutputs(symbol.getTicker(), from, to, period, marketDivCode, adjFlag);
        } catch (RestClientException e) {
            log.warn("[KIS] 차트 API 호출 실패 symbol={} : {}", symbol.getTicker(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "차트 데이터를 가져오는데 실패했습니다.");
        }

        List<StockChartPointDto> points = rawOutputs.stream()
                .map(this::toPoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        StockChartPointDto::date,
                        point -> point,
                        (existing, candidate) -> candidate,
                        TreeMap::new
                ))
                .values().stream()
                .sorted(Comparator.comparing(StockChartPointDto::date))
                .toList();

        return new StockChartDto(
                symbol.getId(),
                symbol.getTicker(),
                symbol.getMarket(),
                from,
                to,
                period,
                points
        );
    }

    private List<KisDailyChartPriceResponse.Output2> fetchChartOutputs(String ticker,
                                                                       LocalDate from,
                                                                       LocalDate to,
                                                                       String period,
                                                                       String marketDivCode,
                                                                       String adjFlag) {
        List<KisDailyChartPriceResponse.Output2> aggregated = new ArrayList<>();

        boolean requiresChunking = "D".equalsIgnoreCase(period)
                && ChronoUnit.DAYS.between(from, to) >= MAX_RECORDS_PER_REQUEST;

        if (!requiresChunking) {
            KisDailyChartPriceResponse response = marketDataClient.fetchDailyChart(
                    ticker, from, to, period, marketDivCode, adjFlag
            );
            validateResponse(response);
            if (response.output2() != null) {
                aggregated.addAll(response.output2());
            }
            return aggregated;
        }

        LocalDate segmentEnd = to;
        while (!segmentEnd.isBefore(from)) {
            LocalDate segmentStart = segmentEnd.minusDays(MAX_RECORDS_PER_REQUEST - 1L);
            if (segmentStart.isBefore(from)) {
                segmentStart = from;
            }

            KisDailyChartPriceResponse response = marketDataClient.fetchDailyChart(
                    ticker, segmentStart, segmentEnd, period, marketDivCode, adjFlag
            );
            validateResponse(response);
            if (response.output2() != null) {
                aggregated.addAll(response.output2());
            }

            segmentEnd = segmentStart.minusDays(1);
        }

        return aggregated;
    }

    private void validateResponse(KisDailyChartPriceResponse response) {
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KIS 차트 응답이 비어 있습니다.");
        }

        if (!"0".equals(response.rtCd())) {
            String message = response.msg() != null ? response.msg() : "KIS API 오류";
            log.warn("[KIS] 차트 조회 실패 rt_cd={} msg_cd={} msg={}", response.rtCd(), response.msgCd(), message);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
        }
    }

    private StockChartPointDto toPoint(KisDailyChartPriceResponse.Output2 output) {
        LocalDate date = parseDate(output.businessDate());
        Long close = parseLong(output.closePrice());
        if (date == null || close == null) {
            return null;
        }
        Long open = parseLong(output.openPrice());
        Long high = parseLong(output.highPrice());
        Long low = parseLong(output.lowPrice());
        Long volume = parseLong(output.accumulatedVolume());
        Long amount = parseLong(output.accumulatedTransactionAmount());

        return new StockChartPointDto(date, close, open, high, low, volume, amount);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), RESPONSE_DATE_FORMAT);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.debug("[KIS] 숫자 파싱 실패 value={}", value);
            return null;
        }
    }
}
