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
        ItemStack stack = new ItemStack(item.material(), amount);
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
        plugin.msg(player, plugin.message("bought").replace("%amount%", String.valueOf(amount)).replace("%item%", pretty(item.material().name())).replace("%price%", plugin.money(total)));
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
        if (total < 0D) return false;
        remove(player, item, amount);
        if (!economy.deposit(player, total)) {
            player.getInventory().addItem(new ItemStack(item.material(), amount));
            return false;
        }
        plugin.msg(player, plugin.message("sold").replace("%amount%", String.valueOf(amount)).replace("%item%", pretty(item.material().name())).replace("%price%", plugin.money(total)));
        return true;
    }

    public int count(Player player, ShopItem item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == item.material()) count += stack.getAmount();
        }
        return count;
    }

    public int sellAll(Player player, ShopItem item) {
        int count = count(player, item);
        if (count > 0) sell(player, item, count);
        return count;
    }

    private void remove(Player player, ShopItem item, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != item.material()) continue;
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

    private static String pretty(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
