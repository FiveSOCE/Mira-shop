package com.mira.shop.gui;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.ShopCatalog;
import com.mira.shop.service.TransactionService;
import com.mira.shop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ShopGuiService {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final TransactionService transactions;

    public ShopGuiService(MiraShopPlugin plugin, ShopCatalog catalog, TransactionService transactions) {
        this.plugin = plugin; this.catalog = catalog; this.transactions = transactions;
    }

    public void openMain(Player player) {
        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN, "", "");
        Inventory inv = Bukkit.createInventory(holder, 54, Text.c(plugin.getConfig().getString("shop.title", "&5Mira Shop")));
        holder.bind(inv);
        List<ShopSection> visible = visibleSections(player);
        for (int slot = 0; slot < visible.size() && slot < 45; slot++) {
            ShopSection section = visible.get(slot);
            inv.setItem(slot, button(section.icon(), section.displayName(), List.of("&7Click to browse")));
        }
        inv.setItem(49, button(Material.GOLD_INGOT, "&eBalance", List.of("&7Use your economy balance to buy items.")));
        player.openInventory(inv);
    }

    public void openSection(Player player, ShopSection section) {
        ShopHolder holder = new ShopHolder(ShopHolder.Type.SECTION, section.id(), "");
        Inventory inv = Bukkit.createInventory(holder, 54, Text.c(section.displayName()));
        holder.bind(inv);
        int slot = 0;
        for (ShopItem item : section.items()) {
            if (slot >= 45) break;
            inv.setItem(slot++, display(item));
        }
        inv.setItem(49, button(Material.ARROW, "&cBack", List.of()));
        player.openInventory(inv);
    }

    public void openTransaction(Player player, String sectionId, ShopItem item) {
        ShopHolder holder = new ShopHolder(ShopHolder.Type.TRANSACTION, sectionId, item.id());
        Inventory inv = Bukkit.createInventory(holder, 27, Text.c("&5" + pretty(item.material().name())));
        holder.bind(inv);
        inv.setItem(13, display(item));
        inv.setItem(10, button(Material.LIME_STAINED_GLASS_PANE, "&aBuy 1", List.of("&7Cost: &f" + price(item.buyPrice(), 1, item.canBuy()))));
        inv.setItem(11, button(Material.LIME_STAINED_GLASS_PANE, "&aBuy 16", List.of("&7Cost: &f" + price(item.buyPrice(), 16, item.canBuy()))));
        inv.setItem(12, button(Material.LIME_STAINED_GLASS_PANE, "&aBuy 64", List.of("&7Cost: &f" + price(item.buyPrice(), 64, item.canBuy()))));
        inv.setItem(14, button(Material.RED_STAINED_GLASS_PANE, "&cSell 1", List.of("&7Value: &f" + price(item.sellPrice(), 1, item.canSell()))));
        inv.setItem(15, button(Material.RED_STAINED_GLASS_PANE, "&cSell 16", List.of("&7Value: &f" + price(item.sellPrice(), 16, item.canSell()))));
        inv.setItem(16, button(Material.RED_STAINED_GLASS_PANE, "&cSell All", List.of("&7You have sellable: &f" + transactions.count(player, item))));
        inv.setItem(22, button(Material.ARROW, "&cBack", List.of()));
        player.openInventory(inv);
    }

    public void handle(Player player, ShopHolder holder, int slot) {
        if (holder.type() == ShopHolder.Type.MAIN) {
            List<ShopSection> visible = visibleSections(player);
            if (slot < 0 || slot >= visible.size() || slot >= 45) return;
            openSection(player, visible.get(slot));
            return;
        }
        if (holder.type() == ShopHolder.Type.SECTION) {
            if (slot == 49) { openMain(player); return; }
            ShopSection section = catalog.section(holder.section()).orElse(null);
            if (section == null || slot < 0 || slot >= section.items().size() || slot >= 45) return;
            openTransaction(player, section.id(), section.items().get(slot));
            return;
        }
        ShopSection section = catalog.section(holder.section()).orElse(null);
        if (section == null) return;
        ShopItem item = section.items().stream().filter(x -> x.id().equals(holder.item())).findFirst().orElse(null);
        if (item == null) return;
        switch (slot) {
            case 10 -> transactions.buy(player, item, 1);
            case 11 -> transactions.buy(player, item, 16);
            case 12 -> transactions.buy(player, item, 64);
            case 14 -> transactions.sell(player, item, 1);
            case 15 -> transactions.sell(player, item, 16);
            case 16 -> transactions.sellAll(player, item);
            case 22 -> openSection(player, section);
            default -> { return; }
        }
        if (slot != 22) openTransaction(player, section.id(), item);
    }

    private List<ShopSection> visibleSections(Player player) {
        List<ShopSection> visible = new ArrayList<>();
        for (ShopSection section : catalog.sections()) {
            if (player.hasPermission("mirashop.section.*") || player.hasPermission("mirashop.section." + section.id())) visible.add(section);
        }
        return visible;
    }

    private String price(double unit, int amount, boolean enabled) {
        return enabled ? plugin.money(unit * amount) : "Disabled";
    }

    private ItemStack display(ShopItem item) {
        return button(item.material(), "&f" + pretty(item.material().name()), List.of(
                item.canBuy() ? "&aBuy: &f" + plugin.money(item.buyPrice()) : "&cNot purchasable",
                item.canSell() ? "&cSell: &f" + plugin.money(item.sellPrice()) : "&7Not sellable",
                "", "&eClick to trade"
        ));
    }

    private static ItemStack button(Material material, String name, List<String> lore) {
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
}
