package com.mira.shop.api;

import org.bukkit.Material;

import java.util.Map;

public interface MaterialPriceService {
    Map<Material, Double> buyPrices();
    Map<Material, Double> sellPrices();

    default double buyPrice(Material material) {
        return buyPrices().getOrDefault(material, -1D);
    }

    default double sellPrice(Material material) {
        return sellPrices().getOrDefault(material, -1D);
    }
}
