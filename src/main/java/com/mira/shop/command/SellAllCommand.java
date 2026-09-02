package com.mira.shop.command;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.service.ShopCatalog;
import com.mira.shop.service.TransactionService;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class SellAllCommand implements CommandExecutor {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final TransactionService transactions;

    public SellAllCommand(MiraShopPlugin plugin, ShopCatalog catalog, TransactionService transactions) {
        this.plugin = plugin; this.catalog = catalog; this.transactions = transactions;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("mirashop.sellall")) { plugin.msg(player, plugin.message("no-permission")); return true; }
        if (args.length == 0) { plugin.msg(player, "&cUsage: /sellall <hand|inventory|material>"); return true; }
        String mode = args[0].toLowerCase(Locale.ROOT);
        if (mode.equals("hand")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) { plugin.msg(player, plugin.message("nothing-to-sell")); return true; }
            ShopItem item = catalog.findByMaterial(hand.getType()).orElse(null);
            if (item == null || !item.canSell()) { plugin.msg(player, plugin.message("nothing-to-sell")); return true; }
            transactions.sellAll(player, item);
            return true;
        }
        if (mode.equals("inventory")) {
            Set<Material> handled = new HashSet<>();
            boolean sold = false;
            for (ItemStack stack : player.getInventory().getStorageContents()) {
                if (stack == null || stack.getType().isAir() || !handled.add(stack.getType())) continue;
                ShopItem item = catalog.findByMaterial(stack.getType()).orElse(null);
                if (item != null && item.canSell() && transactions.sellAll(player, item) > 0) sold = true;
            }
            if (!sold) plugin.msg(player, plugin.message("nothing-to-sell"));
            return true;
        }
        Material material = Material.matchMaterial(args[0]);
        if (material == null) { plugin.msg(player, "&cUnknown material."); return true; }
        ShopItem item = catalog.findByMaterial(material).orElse(null);
        if (item == null || !item.canSell()) { plugin.msg(player, plugin.message("nothing-to-sell")); return true; }
        transactions.sellAll(player, item);
        return true;
    }
}
