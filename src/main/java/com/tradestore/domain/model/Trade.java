package com.tradestore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trades")
public class Trade {
    @Id
    private String id;

    @Field("trade_id")
    private TradeId tradeId;

    @Field("counter_party_id")
    private String counterPartyId;

    @Field("book_id")
    private String bookId;

    @Field("maturity_date")
    private LocalDate maturityDate;

    @Field("created_date")
    private LocalDate createdDate;

    @Field("expired")
    private boolean expired;

    public String getStringId() {
        return tradeId != null ? tradeId.getTradeId() : null;
    }
} 