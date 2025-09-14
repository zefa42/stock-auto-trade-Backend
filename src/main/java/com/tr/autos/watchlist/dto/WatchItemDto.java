package com.tr.autos.watchlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WatchItemDto {
    private Long symbolId;
    private String ticker;
    private String market;
    private String name;
}