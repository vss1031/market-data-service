package com.example.marketdata.service;

import com.example.marketdata.model.MarketTicker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class OkxMarketService {
    private final WebClient client;
    private final ObjectMapper mapper;

    public OkxMarketService(@Value("${okx.rest-url}") String url, ObjectMapper mapper) {
        this.client = WebClient.builder().baseUrl(url).build(); this.mapper = mapper;
    }

    public Mono<List<MarketTicker>> top20Spot() {
        return client.get().uri("/api/v5/market/tickers?instType=SPOT").retrieve().bodyToMono(String.class)
            .map(this::parse).map(list -> list.stream().sorted(Comparator.comparing(x -> safeDecimal(x.volume24h()), Comparator.reverseOrder())).limit(20).toList());
    }

    private List<MarketTicker> parse(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            if (!"0".equals(root.path("code").asText())) throw new IllegalStateException("OKX returned code " + root.path("code").asText());
            return java.util.stream.StreamSupport.stream(root.path("data").spliterator(), false)
                .filter(n -> n.path("instId").asText().contains("-"))
                .map(n -> new MarketTicker(n.path("instId").asText(), n.path("last").asText(),
                    percent(n.path("last").asText(), n.path("open24h").asText()), n.path("volCcy24h").asText())).toList();
        } catch (Exception e) { throw new IllegalStateException("Unable to parse OKX ticker response", e); }
    }
    private static BigDecimal safeDecimal(String s) { try { return new BigDecimal(s == null || s.isBlank() ? "0" : s); } catch (Exception e) { return BigDecimal.ZERO; } }
    private static String percent(String last, String open) {
        BigDecimal l=safeDecimal(last), o=safeDecimal(open);
        if (o.signum()==0) return "0";
        return l.subtract(o).multiply(BigDecimal.valueOf(100)).divide(o, 4, RoundingMode.HALF_UP).toPlainString();
    }
}
