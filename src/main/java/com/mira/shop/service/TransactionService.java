package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public final class TransactionService {
    private final MiraShopPlugin plugin;
    private final EconomyService economy;

    public TransactionService(MiraShopPlugin plugin, EconomyService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public boolean buy(Player player, ShopItem item, int amount) {
        if (!player.hasPermission("mirashop.buy") || !item.canBuy() || amount <= 0) return false;
        double total = safeTotal(item.buyPrice(), amount);
        if (total < 0D || economy.balance(player) + 0.0001D < total) {
            plugin.msg(player, plugin.message("insufficient-funds"));
            return false;
        }
        ItemStack stack = item.create(amount);
        if (!hasSpace(player, stack)) {
            plugin.msg(player, plugin.message("inventory-full"));
            return false;
        }
        if (!economy.withdraw(player, total)) return false;
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        if (!leftovers.isEmpty()) {
            economy.deposit(player, total);
            for (ItemStack leftover : leftovers.values()) player.getInventory().removeItem(leftover);
            plugin.msg(player, plugin.message("inventory-full"));
            return false;
        }
        plugin.stats().recordBuy(item, amount, total);
        plugin.msg(player, plugin.message("bought").replace("%amount%", String.valueOf(amount)).replace("%item%", displayName(item)).replace("%price%", plugin.money(total)));
        return true;
    }

    public boolean sell(Player player, ShopItem item, int requested) {
        if (!player.hasPermission("mirashop.sell") || !item.canSell() || requested <= 0) return false;
        int available = count(player, item);
        int amount = Math.min(requested, available);
        if (amount <= 0) {
            plugin.msg(player, plugin.message("nothing-to-sell"));
            return false;
        }
        double total = safeTotal(item.sellPrice(), amount);
        if (total < 0D || !economy.deposit(player, total)) return false;
        remove(player, item, amount);
        plugin.stats().recordSell(item, amount, total);
        plugin.msg(player, plugin.message("sold").replace("%amount%", String.valueOf(amount)).replace("%item%", displayName(item)).replace("%price%", plugin.money(total)));
        return true;
    }

    public int count(Player player, ShopItem item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) if (isSellableStack(stack, item)) count += stack.getAmount();
        return count;
    }

    public int sellAll(Player player, ShopItem item) {
        int count = count(player, item);
        if (count > 0) sell(player, item, count);
        return count;
    }

    public boolean sellPlayerSlot(Player player, int playerSlot) {
        if (!player.hasPermission("mirashop.sell")) return false;
        ItemStack stack = player.getInventory().getItem(playerSlot);
        if (stack == null || stack.getType().isAir()) return false;
        ShopItem item = plugin.catalog().findByStack(stack).orElse(null);
        if (item == null || !isSellableStack(stack, item)) return false;
        double total = safeTotal(item.sellPrice(), stack.getAmount());
        if (total < 0D || !economy.deposit(player, total)) return false;
        ItemStack sold = stack.clone();
        player.getInventory().setItem(playerSlot, null);
        plugin.stats().recordSell(item, sold.getAmount(), total);
        plugin.msg(player, plugin.message("sold").replace("%amount%", String.valueOf(sold.getAmount())).replace("%item%", displayName(item)).replace("%price%", plugin.money(total)));
        return true;
    }

    public void sellEligibleInventory(Player player) {
        if (!player.hasPermission("mirashop.sell")) return;
        double total = 0D;
        int sold = 0;
        ItemStack[] original = player.getInventory().getStorageContents();
        ItemStack[] result = original.clone();
        java.util.Map<ShopItem, Integer> byItem = new java.util.HashMap<>();
        for (int i = 0; i < original.length; i++) {
            ItemStack stack = original[i];
            if (stack == null || stack.getType().isAir()) continue;
            ShopItem item = plugin.catalog().findByStack(stack).orElse(null);
            if (item == null || !isSellableStack(stack, item)) continue;
            double value = safeTotal(item.sellPrice(), stack.getAmount());
            if (value < 0D) continue;
            total += value;
            sold += stack.getAmount();
            byItem.merge(item, stack.getAmount(), Integer::sum);
            result[i] = null;
        }
        if (sold == 0) {
            plugin.msg(player, plugin.message("nothing-to-sell"));
            return;
        }
        if (!economy.deposit(player, total)) return;
        player.getInventory().setStorageContents(result);
        byItem.forEach((item, amount) -> plugin.stats().recordSell(item, amount, item.sellPrice() * amount));
        plugin.msg(player, "&aSold &f" + sold + "&a items for &f" + plugin.money(total) + "&a.");
    }

    public boolean isSellableStack(ItemStack stack, ShopItem item) {
        if (stack == null || !item.canSell() || !plugin.catalog().matches(stack, item)) return false;
        if (item.customTemplate()) return true;
        if (stack.hasItemMeta()) {
            var meta = stack.getItemMeta();
            if (meta.hasDisplayName() || meta.hasCustomName()) return false;
            if (!meta.getPersistentDataContainer().isEmpty()) return false;
            if (meta.hasEnchants() || meta.hasCustomModelData()) return false;
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable && damageable.hasDamage()) return false;
        }
        return true;
    }

    private void remove(Player player, ShopItem item, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (!isSellableStack(stack, item)) continue;
            int take = Math.min(left, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            if (stack.getAmount() <= 0) contents[i] = null;
            left -= take;
        }
        player.getInventory().setStorageContents(contents);
    }

    private boolean hasSpace(Player player, ItemStack adding) {
        int left = adding.getAmount();
        int max = adding.getMaxStackSize();
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null || stack.getType().isAir()) left -= max;
            else if (stack.isSimilar(adding)) left -= Math.max(0, max - stack.getAmount());
            if (left <= 0) return true;
        }
        return false;
    }

    private static double safeTotal(double price, int amount) {
        if (!Double.isFinite(price) || price < 0D || amount <= 0) return -1D;
        double total = price * amount;
        return Double.isFinite(total) && total >= 0D ? total : -1D;
    }

    private static String displayName(ShopItem item) {
        String id = item.id().replace('_', ' ');
        StringBuilder out = new StringBuilder();
        for (String part : id.split(" ")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
