package com.mira.shop.service;

import com.mira.shop.api.MaterialPriceService;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

public final class CachedMaterialPriceService implements MaterialPriceService {
    private volatile Map<Material, Double> buyPrices = Map.of();
    private volatile Map<Material, Double> sellPrices = Map.of();

    public void rebuild(ShopCatalog catalog) {
        EnumMap<Material, Double> buys = new EnumMap<>(Material.class);
        EnumMap<Material, Double> sells = new EnumMap<>(Material.class);

        for (ShopSection section : catalog.sections()) {
            for (ShopItem item : section.items()) {
                if (item.customTemplate() || item.material() == Material.SPAWNER) continue;
                if (item.canBuy()) buys.merge(item.material(), item.buyPrice(), Math::max);
                if (item.canSell()) sells.merge(item.material(), item.sellPrice(), Math::max);
            }
        }

        buyPrices = Map.copyOf(buys);
        sellPrices = Map.copyOf(sells);
    }

    @Override
    public Map<Material, Double> buyPrices() {
        return buyPrices;
    }

    @Override
    public Map<Material, Double> sellPrices() {
        return sellPrices;
    }
}
