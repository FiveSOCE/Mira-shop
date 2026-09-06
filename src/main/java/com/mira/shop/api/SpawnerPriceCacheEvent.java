package com.mira.shop.api;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class SpawnerPriceCacheEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Map<EntityType, Double> prices;

    public SpawnerPriceCacheEvent(Map<EntityType, Double> prices) {
        this.prices = prices == null ? Map.of() : Map.copyOf(prices);
    }

    public Map<EntityType, Double> prices() {
        return prices;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
