package com.mira.shop.model;

import org.bukkit.Material;

public record ShopItem(String id, Material material, double buyPrice, double sellPrice) {
    public boolean canBuy() { return buyPrice >= 0; }
    public boolean canSell() { return sellPrice >= 0; }
}
