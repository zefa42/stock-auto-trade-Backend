package com.tr.autos.watchlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddWatchRequest {
    @NotNull
    private Long symbolId;
}