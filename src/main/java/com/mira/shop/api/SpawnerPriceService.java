package com.mira.shop.api;

import org.bukkit.entity.EntityType;

import java.util.Map;

public interface SpawnerPriceService {
    Map<EntityType, Double> buyPrices();

    default double buyPrice(EntityType type) {
        if (type == null) return -1D;
        return buyPrices().getOrDefault(type, -1D);
    }
}
