package com.tradestore.infrastructure.mapper;

import com.tradestore.domain.model.Trade;
import com.tradestore.infrastructure.entity.TradeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    
    @Mapping(source = "tradeId.tradeId", target = "tradeId")
    @Mapping(source = "tradeId.version", target = "version")
    TradeEntity toEntity(Trade trade);

    @Mapping(target = "tradeId.tradeId", source = "tradeId")
    @Mapping(target = "tradeId.version", source = "version")
    Trade toDomain(TradeEntity entity);
} 