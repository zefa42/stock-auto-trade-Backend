package com.tr.autos.market.kis.service;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.market.dto.DomesticMarketCapRankingItem;
import com.tr.autos.market.kis.KisMarketRankingClient;
import com.tr.autos.market.kis.dto.KisMarketCapRankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomesticMarketRankingService {

    private final KisMarketRankingClient rankingClient;
    private final SymbolRepository symbolRepository;

    public List<DomesticMarketCapRankingItem> fetchMarketCapRanking() {
        KisMarketCapRankingResponse response = rankingClient.fetchMarketCapRanking();

        if (response == null || response.output() == null) {
            log.warn("[KIS] 시가총액 순위 응답이 비어있습니다.");
            return List.of();
        }

        if (!"0".equals(response.rtCd())) {
            String message = String.format("KIS API 오류 (code=%s, msg=%s)", response.msgCd(), response.message());
            log.warn("[KIS] {}", message);
            throw new IllegalStateException(message);
        }

        Set<String> tickers = response.output().stream()
                .map(KisMarketCapRankingResponse.Entry::mkscShrnIscd)
                .map(this::normalizeTicker)
                .collect(Collectors.toSet());

        Map<String, Symbol> symbolMap = tickers.isEmpty()
                ? Collections.emptyMap()
                : symbolRepository.findByTickerIn(tickers).stream()
                .collect(Collectors.toMap(
                        symbol -> normalizeTicker(symbol.getTicker()),
                        Function.identity(),
                        (left, right) -> left
                ));

        return response.output().stream()
                .map(entry -> toDto(entry, symbolMap.get(normalizeTicker(entry.mkscShrnIscd()))))
                .collect(Collectors.toList());
    }

    private DomesticMarketCapRankingItem toDto(KisMarketCapRankingResponse.Entry entry, Symbol symbol) {
        return new DomesticMarketCapRankingItem(
                parseInt(entry.dataRank()),
                symbol != null ? symbol.getId() : null,
                "KRX",
                normalizeTicker(entry.mkscShrnIscd()),
                entry.htsKorIsnm(),
                parseLong(entry.stckPrpr()),
                parseLong(entry.prdyVrss()),
                entry.prdyVrssSign(),
                parseDecimal(entry.prdyCtrt()),
                parseLong(entry.acmlVol()),
                parseLong(entry.lstnStcn()),
                parseLong(entry.stckAvls()),
                parseDecimal(entry.mrktWholAvlsRlim())
        );
    }

    private String normalizeTicker(String ticker) {
        return ticker == null ? "" : ticker.trim();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}

