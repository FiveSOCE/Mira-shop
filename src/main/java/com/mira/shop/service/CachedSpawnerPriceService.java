package com.mira.shop.service;

import com.mira.shop.api.SpawnerPriceService;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;

public final class CachedSpawnerPriceService implements SpawnerPriceService {
    private volatile Map<EntityType, Double> prices = Map.of();

    public void rebuild(ShopCatalog catalog) {
        EnumMap<EntityType, Double> next = new EnumMap<>(EntityType.class);
        if (catalog != null) {
            for (ShopSection section : catalog.sections()) {
                for (ShopItem item : section.items()) {
                    String typeName = catalog.spawnerType(item.template());
                    if (typeName == null || item.buyPrice() < 0D) continue;
                    try {
                        next.put(EntityType.valueOf(typeName), item.buyPrice());
                    } catch (IllegalArgumentException ignored) { }
                }
            }
        }
        prices = Map.copyOf(next);
    }

    @Override
    public Map<EntityType, Double> buyPrices() {
        return prices;
    }
}
