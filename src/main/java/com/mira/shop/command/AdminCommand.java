package com.mira.shop.command;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.service.ShopCatalog;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class AdminCommand implements CommandExecutor {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;

    public AdminCommand(MiraShopPlugin plugin, ShopCatalog catalog) {
        this.plugin = plugin; this.catalog = catalog;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mirashop.admin")) { plugin.msg(sender, plugin.message("no-permission")); return true; }
        if (args.length == 0) {
            plugin.msg(sender, "&e/mshop reload");
            plugin.msg(sender, "&e/mshop setprice <section> <item> <buy|sell> <price|-1>");
            plugin.msg(sender, "&e/mshop addhand <section> <id> <buy> <sell>");
            plugin.msg(sender, "&e/mshop remove <section> <item>");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            plugin.msg(sender, plugin.message("reloaded"));
            return true;
        }
        try {
            if (args[0].equalsIgnoreCase("setprice") && args.length >= 5) {
                String section = args[1].toLowerCase(Locale.ROOT);
                String item = args[2].toLowerCase(Locale.ROOT);
                double value = Double.parseDouble(args[4]);
                if (!Double.isFinite(value) || value < -1D) throw new IllegalArgumentException();
                if (args[3].equalsIgnoreCase("buy")) catalog.setPrice(section, item, value, null);
                else if (args[3].equalsIgnoreCase("sell")) catalog.setPrice(section, item, null, value);
                else throw new IllegalArgumentException();
                plugin.msg(sender, "&aPrice updated.");
                return true;
            }
            if (args[0].equalsIgnoreCase("addhand") && args.length >= 5 && sender instanceof Player player) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) { plugin.msg(sender, "&cHold an item first."); return true; }
                String section = args[1].toLowerCase(Locale.ROOT);
                String id = args[2].toLowerCase(Locale.ROOT).replace(' ', '_');
                double buy = Double.parseDouble(args[3]);
                double sell = Double.parseDouble(args[4]);
                catalog.addHandItem(section, id, hand.getType(), buy, sell);
                plugin.msg(sender, "&aShop item added from your hand.");
                return true;
            }
            if (args[0].equalsIgnoreCase("remove") && args.length >= 3) {
                catalog.removeItem(args[1].toLowerCase(Locale.ROOT), args[2].toLowerCase(Locale.ROOT));
                plugin.msg(sender, "&aShop item removed.");
                return true;
            }
        } catch (Exception ex) {
            plugin.msg(sender, "&cInvalid shop edit input.");
            return true;
        }
        plugin.msg(sender, "&cUnknown MiraShop admin command.");
        return true;
    }
}
