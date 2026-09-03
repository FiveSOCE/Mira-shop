package com.mira.shop.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record ShopItem(String id, ItemStack template, double buyPrice, double sellPrice) {
    public ShopItem {
        template = template == null ? new ItemStack(Material.STONE) : template.clone();
        template.setAmount(1);
    }

    public Material material() { return template.getType(); }
    public boolean canBuy() { return buyPrice >= 0 && (sellPrice < 0 || buyPrice >= sellPrice); }
    public boolean canSell() { return sellPrice >= 0; }

    public ItemStack create(int amount) {
        ItemStack out = template.clone();
        out.setAmount(Math.max(1, amount));
        return out;
    }

    public boolean customTemplate() {
        if (!template.hasItemMeta()) return false;
        var meta = template.getItemMeta();
        return meta.hasDisplayName() || meta.hasCustomName() || !meta.getPersistentDataContainer().isEmpty()
                || meta.hasEnchants() || meta.hasCustomModelData();
    }
}
