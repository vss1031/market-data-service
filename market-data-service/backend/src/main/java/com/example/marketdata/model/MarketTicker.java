package com.example.marketdata.model;

public record MarketTicker(String symbol, String lastPrice, String change24hPercent, String volume24h) {}
