package com.example.marketdata.controller;

import com.example.marketdata.model.MarketTicker;
import com.example.marketdata.service.OkxMarketService;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/markets")
public class MarketController {
    private final OkxMarketService service;
    public MarketController(OkxMarketService service) { this.service = service; }
    @GetMapping("/top20") public Mono<List<MarketTicker>> top20() { return service.top20Spot(); }
}
