package com.mira.shop.gui;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.service.ShopCatalog;
import com.mira.shop.service.TransactionService;
import com.mira.shop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class SellGuiService {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final TransactionService transactions;

    public SellGuiService(MiraShopPlugin plugin, ShopCatalog catalog, TransactionService transactions) {
        this.plugin = plugin;
        this.catalog = catalog;
        this.transactions = transactions;
    }

    public void open(Player player) {
        SellHolder holder = new SellHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, Text.c("&5Sell Items"));
        holder.bind(inv);

        ItemStack[] storage = player.getInventory().getStorageContents();
        for (int guiSlot = 0; guiSlot < 27; guiSlot++) {
            int playerSlot = guiSlot + 9;
            ItemStack live = playerSlot < storage.length ? storage[playerSlot] : null;
            if (live == null || live.getType().isAir()) {
                inv.setItem(guiSlot, filler());
                continue;
            }

            ShopItem shopItem = catalog.findByMaterial(live.getType()).orElse(null);
            if (shopItem == null || !transactions.isSellableStack(live, shopItem)) {
                inv.setItem(guiSlot, blocked(live));
                continue;
            }

            ItemStack shown = live.clone();
            ItemMeta meta = shown.getItemMeta();
            var lore = meta.lore();
            java.util.ArrayList<net.kyori.adventure.text.Component> lines = lore == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(lore);
            lines.add(Text.c(""));
            lines.add(Text.c("&aClick to sell this stack"));
            lines.add(Text.c("&7Value: &f" + plugin.money(shopItem.sellPrice() * live.getAmount())));
            meta.lore(lines);
            shown.setItemMeta(meta);
            inv.setItem(guiSlot, shown);
        }

        for (int slot = 27; slot < 36; slot++) inv.setItem(slot, filler());
        inv.setItem(31, button(Material.EMERALD, "&aSell Inventory", List.of("&7Sell every eligible item in your inventory.", "&cNamed and protected custom items are always skipped.")));
        player.openInventory(inv);
    }

    public void handle(Player player, int slot) {
        if (slot == 31) {
            transactions.sellEligibleInventory(player);
            open(player);
            return;
        }
        if (slot < 0 || slot >= 27) return;
        int playerSlot = slot + 9;
        transactions.sellPlayerSlot(player, playerSlot);
        open(player);
    }

    private ItemStack blocked(ItemStack original) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        String itemName = original.hasItemMeta() && original.getItemMeta().hasDisplayName()
                ? "Protected / named item"
                : "Not sellable";
        meta.displayName(Text.c("&c" + itemName));
        meta.lore(List.of(Text.c("&7" + pretty(original.getType().name())), Text.c("&cThis item cannot be sold here.")));
        barrier.setItemMeta(meta);
        return barrier;
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.c(" "));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.c(name));
        meta.lore(lore.stream().map(Text::c).toList());
        item.setItemMeta(meta);
        return item;
    }

    private static String pretty(String input) {
        StringBuilder out = new StringBuilder();
        for (String part : input.toLowerCase().split("_")) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    public static final class SellHolder implements InventoryHolder {
        private Inventory inventory;
        private void bind(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
