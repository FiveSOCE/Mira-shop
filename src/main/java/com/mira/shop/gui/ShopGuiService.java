package com.mira.shop.gui;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.EconomyService;
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
    private static final List<Integer> MAIN_SECTION_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16);

    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final TransactionService transactions;
    private final EconomyService economy;

    public ShopGuiService(MiraShopPlugin plugin, ShopCatalog catalog, TransactionService transactions, EconomyService economy) {
        this.plugin = plugin;
        this.catalog = catalog;
        this.transactions = transactions;
        this.economy = economy;
    }

    public void openMain(Player player) {
        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN, "", "");
        Inventory inv = Bukkit.createInventory(holder, 27, Text.c(plugin.getConfig().getString("shop.title", "&5Mira Shop")));
        holder.bind(inv);
        fill(inv);

        List<ShopSection> visible = visibleSections(player);
        for (int i = 0; i < visible.size() && i < MAIN_SECTION_SLOTS.size(); i++) {
            ShopSection section = visible.get(i);
            inv.setItem(MAIN_SECTION_SLOTS.get(i), button(section.icon(), section.displayName(), List.of("&7Click to browse")));
        }

        inv.setItem(22, button(Material.BARRIER, "&cClose", List.of()));
        inv.setItem(26, button(Material.GOLD_INGOT, "&eBalance", List.of("&7Current balance: &f" + plugin.money(economy.balance(player)))));
        player.openInventory(inv);
    }

    public void openSection(Player player, ShopSection section) {
        int rows = Math.max(3, Math.min(6, ((section.items().size() + 6) / 7) + 2));
        int size = rows * 9;
        ShopHolder holder = new ShopHolder(ShopHolder.Type.SECTION, section.id(), "");
        Inventory inv = Bukkit.createInventory(holder, size, Text.c(section.displayName()));
        holder.bind(inv);
        fill(inv);

        List<Integer> slots = contentSlots(size);
        for (int i = 0; i < section.items().size() && i < slots.size(); i++) {
            inv.setItem(slots.get(i), display(section.items().get(i)));
        }

        inv.setItem(size - 5, button(Material.ARROW, "&cBack", List.of()));
        player.openInventory(inv);
    }

    public void openTransaction(Player player, String sectionId, ShopItem item) {
        ShopHolder holder = new ShopHolder(ShopHolder.Type.TRANSACTION, sectionId, item.id());
        Inventory inv = Bukkit.createInventory(holder, 27, Text.c("&5" + pretty(item.material().name())));
        holder.bind(inv);
        fill(inv);

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
            if (slot == 22) {
                player.closeInventory();
                return;
            }
            int index = MAIN_SECTION_SLOTS.indexOf(slot);
            if (index < 0) return;
            List<ShopSection> visible = visibleSections(player);
            if (index >= visible.size()) return;
            openSection(player, visible.get(index));
            return;
        }

        if (holder.type() == ShopHolder.Type.SECTION) {
            ShopSection section = catalog.section(holder.section()).orElse(null);
            if (section == null) return;
            int backSlot = player.getOpenInventory().getTopInventory().getSize() - 5;
            if (slot == backSlot) {
                openMain(player);
                return;
            }
            List<Integer> slots = contentSlots(player.getOpenInventory().getTopInventory().getSize());
            int index = slots.indexOf(slot);
            if (index < 0 || index >= section.items().size()) return;
            openTransaction(player, section.id(), section.items().get(index));
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

    private List<Integer> contentSlots(int size) {
        int rows = size / 9;
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col <= 7; col++) slots.add(row * 9 + col);
        }
        return slots;
    }

    private void fill(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Text.c(" "));
        meta.setEnchantmentGlintOverride(true);
        filler.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
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
