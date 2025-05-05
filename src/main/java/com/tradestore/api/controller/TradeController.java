package com.tradestore.api.controller;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade Controller", description = "APIs for managing trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    @Operation(summary = "Store a new trade")
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        Trade savedTrade = tradeService.storeTrade(trade);
        return new ResponseEntity<>(savedTrade, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all trades")
    public ResponseEntity<List<Trade>> getAllTrades() {
        List<Trade> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{tradeId}/{version}")
    @Operation(summary = "Get a specific version of a trade")
    public ResponseEntity<Trade> getTrade(@PathVariable String tradeId, @PathVariable Integer version) {
        return tradeService.getTradeById(tradeId, version)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{tradeId}/versions")
    @Operation(summary = "Get all versions of a trade by ID")
    public ResponseEntity<List<Trade>> getTradeVersions(@PathVariable String tradeId) {
        List<Trade> trades = tradeService.getTradesByTradeId(tradeId);
        return trades.isEmpty() 
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(trades);
    }

    @GetMapping("/mongo")
    @Operation(summary = "Get all trades from MongoDB")
    public ResponseEntity<List<Trade>> getMongoTrades() {
        List<Trade> trades = tradeService.getMongoTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/postgres")
    @Operation(summary = "Get all trades from PostgreSQL")
    public ResponseEntity<List<Trade>> getPostgresTrades() {
        List<Trade> trades = tradeService.getPostgresTrades();
        return ResponseEntity.ok(trades);
    }
} 