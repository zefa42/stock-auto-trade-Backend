package com.tr.autos.watchlist.controller;

import com.tr.autos.watchlist.dto.WatchItemDto;
import com.tr.autos.watchlist.dto.request.AddWatchRequest;
import com.tr.autos.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<List<WatchItemDto>> myList(Principal principal){
        return ResponseEntity.ok(watchlistService.myList(principal));
    }

    @PostMapping
    public ResponseEntity<Void> add(Principal principal, @RequestBody @Valid AddWatchRequest req){
        watchlistService.add(principal, req.getSymbolId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{symbolId}")
    public ResponseEntity<Void> remove(Principal principal, @PathVariable Long symbolId){
        watchlistService.remove(principal, symbolId);
        return ResponseEntity.noContent().build();
    }
}