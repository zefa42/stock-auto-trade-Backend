package com.tr.autos.market.kis.controller;

import com.tr.autos.market.dto.DomesticMarketCapRankingItem;
import com.tr.autos.market.kis.service.DomesticMarketRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/markets/domestic")
@RequiredArgsConstructor
public class DomesticMarketRankingController {

    private final DomesticMarketRankingService rankingService;

    @GetMapping("/ranking/market-cap")
    public ResponseEntity<List<DomesticMarketCapRankingItem>> marketCapRanking() {
        return ResponseEntity.ok(rankingService.fetchMarketCapRanking());
    }
}

