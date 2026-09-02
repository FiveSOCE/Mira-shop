package com.mira.shop.gui;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.ShopCatalog;
import com.mira.shop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AdminGuiService {
    public enum Awaiting { BUY, SELL }

    private record Pending(String section, String item, Awaiting type) {}

    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public AdminGuiService(MiraShopPlugin plugin, ShopCatalog catalog) {
        this.plugin = plugin; this.catalog = catalog;
    }

    public void openSections(Player player) {
        AdminHolder holder = new AdminHolder(AdminHolder.Type.SECTIONS, "", "");
        Inventory inv = Bukkit.createInventory(holder, 54, Text.c("&5MiraShop Editor"));
        holder.bind(inv);
        int slot = 0;
        for (ShopSection section : catalog.sections()) {
            if (slot >= 45) break;
            inv.setItem(slot++, button(section.icon(), section.displayName(), List.of("&7ID: &f" + section.id(), "&7Items: &f" + section.items().size(), "", "&eClick to manage")));
        }
        inv.setItem(49, button(Material.BARRIER, "&cClose", List.of()));
        player.openInventory(inv);
    }

    public void openItems(Player player, ShopSection section) {
        AdminHolder holder = new AdminHolder(AdminHolder.Type.ITEMS, section.id(), "");
        Inventory inv = Bukkit.createInventory(holder, 54, Text.c("&5Edit: " + section.displayName()));
        holder.bind(inv);
        int slot = 0;
        for (ShopItem item : section.items()) {
            if (slot >= 45) break;
            inv.setItem(slot++, itemButton(item));
        }
        inv.setItem(45, button(Material.ARROW, "&cBack", List.of()));
        inv.setItem(53, button(Material.EMERALD, "&aAdd Held Item", List.of("&7Adds the material in your main hand", "&7Default buy/sell price: disabled", "&eThen edit it in this menu")));
        player.openInventory(inv);
    }

    public void openItem(Player player, String sectionId, ShopItem item) {
        AdminHolder holder = new AdminHolder(AdminHolder.Type.ITEM_EDIT, sectionId, item.id());
        Inventory inv = Bukkit.createInventory(holder, 27, Text.c("&5Edit Shop Item"));
        holder.bind(inv);
        inv.setItem(11, button(Material.GOLD_INGOT, "&eBuy Price", List.of("&7Current: &f" + (item.canBuy() ? plugin.money(item.buyPrice()) : "Disabled"), "&eClick then enter price in chat", "&7Use -1 to disable buying")));
        inv.setItem(13, itemButton(item));
        inv.setItem(15, button(Material.IRON_INGOT, "&eSell Price", List.of("&7Current: &f" + (item.canSell() ? plugin.money(item.sellPrice()) : "Disabled"), "&eClick then enter price in chat", "&7Use -1 to disable selling")));
        inv.setItem(18, button(Material.ARROW, "&cBack", List.of()));
        inv.setItem(26, button(Material.BARRIER, "&4Remove Item", List.of("&cPermanently removes this shop entry")));
        player.openInventory(inv);
    }

    public void handle(Player player, AdminHolder holder, int slot) {
        if (!player.hasPermission("mirashop.admin")) { player.closeInventory(); return; }
        if (holder.type() == AdminHolder.Type.SECTIONS) {
            if (slot == 49) { player.closeInventory(); return; }
            if (slot < 0 || slot >= 45) return;
            int index = 0;
            for (ShopSection section : catalog.sections()) if (index++ == slot) { openItems(player, section); return; }
            return;
        }
        if (holder.type() == AdminHolder.Type.ITEMS) {
            ShopSection section = catalog.section(holder.section()).orElse(null);
            if (section == null) return;
            if (slot == 45) { openSections(player); return; }
            if (slot == 53) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) { plugin.msg(player, "&cHold the item you want to add first."); return; }
                String id = hand.getType().name().toLowerCase();
                catalog.addHandItem(section.id(), id, hand.getType(), -1D, -1D);
                openItems(player, catalog.section(section.id()).orElse(section));
                return;
            }
            if (slot < 0 || slot >= section.items().size()) return;
            openItem(player, section.id(), section.items().get(slot));
            return;
        }
        ShopSection section = catalog.section(holder.section()).orElse(null);
        if (section == null) return;
        ShopItem item = section.items().stream().filter(x -> x.id().equals(holder.item())).findFirst().orElse(null);
        if (item == null) return;
        if (slot == 11 || slot == 15) {
            Awaiting type = slot == 11 ? Awaiting.BUY : Awaiting.SELL;
            pending.put(player.getUniqueId(), new Pending(section.id(), item.id(), type));
            player.closeInventory();
            plugin.msg(player, "&eType the new " + type.name().toLowerCase() + " price in chat. Use &f-1&e to disable it.");
            return;
        }
        if (slot == 18) { openItems(player, section); return; }
        if (slot == 26) {
            catalog.removeItem(section.id(), item.id());
            plugin.msg(player, "&aShop item removed.");
            openItems(player, catalog.section(section.id()).orElse(section));
        }
    }

    public boolean awaiting(UUID uuid) { return pending.containsKey(uuid); }

    public void acceptChat(Player player, String raw) {
        Pending edit = pending.remove(player.getUniqueId());
        if (edit == null) return;
        try {
            double value = Double.parseDouble(raw.trim());
            if (!Double.isFinite(value) || value < -1D) throw new NumberFormatException();
            if (edit.type() == Awaiting.BUY) catalog.setPrice(edit.section(), edit.item(), value, null);
            else catalog.setPrice(edit.section(), edit.item(), null, value);
            plugin.msg(player, "&aPrice updated.");
        } catch (NumberFormatException ex) {
            plugin.msg(player, "&cInvalid price. Enter a number or -1 to disable.");
        }
        ShopSection section = catalog.section(edit.section()).orElse(null);
        if (section == null) { openSections(player); return; }
        ShopItem item = section.items().stream().filter(x -> x.id().equals(edit.item())).findFirst().orElse(null);
        if (item == null) openItems(player, section); else openItem(player, section.id(), item);
    }

    private ItemStack itemButton(ShopItem item) {
        return button(item.material(), "&f" + item.material().name(), List.of(
                "&aBuy: &f" + (item.canBuy() ? plugin.money(item.buyPrice()) : "Disabled"),
                "&cSell: &f" + (item.canSell() ? plugin.money(item.sellPrice()) : "Disabled"),
                "", "&eClick to edit"
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
}
