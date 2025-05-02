package com.tradestore.api.controller;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade Controller", description = "APIs for managing trades")
public class TradeController {

    private final TradeService tradeService;

    @Autowired
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    @Operation(summary = "Store a new trade")
    public ResponseEntity<Trade> storeTrade(@RequestBody Trade trade) {
        Trade savedTrade = tradeService.storeTrade(trade);
        return new ResponseEntity<>(savedTrade, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all trades")
    public ResponseEntity<List<Trade>> getAllTrades() {
        List<Trade> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{tradeId}")
    @Operation(summary = "Get the latest version of a trade by ID")
    public ResponseEntity<Trade> getTradeById(@PathVariable String tradeId) {
        return tradeService.getTradeById(tradeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{tradeId}/versions")
    @Operation(summary = "Get all versions of a trade by ID")
    public ResponseEntity<List<Trade>> getTradeVersions(@PathVariable String tradeId) {
        List<Trade> trades = tradeService.getTradesByTradeId(tradeId);
        return ResponseEntity.ok(trades);
    }
} 