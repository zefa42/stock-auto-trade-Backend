package com.tr.autos.quote.test;

import com.tr.autos.quote.service.KisIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("local")
public class QuoteRefreshServiceImpl implements QuoteRefreshService {

    private final KisIntegrationService kisIntegrationService;

    @Override
    public int refreshAll() {
        return kisIntegrationService.refreshAllQuotes();
    }
}
