package com.mira.shop.command;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.gui.ShopGuiService;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.ShopCatalog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ShopCommand implements CommandExecutor {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final ShopGuiService gui;

    public ShopCommand(MiraShopPlugin plugin, ShopCatalog catalog, ShopGuiService gui) {
        this.plugin = plugin; this.catalog = catalog; this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("mirashop.use")) { plugin.msg(player, plugin.message("no-permission")); return true; }
        if (args.length == 0) { gui.openMain(player); return true; }

        if (args[0].equalsIgnoreCase("search")) {
            if (args.length < 2) {
                plugin.msg(player, "&eUsage: &f/shop search <item>");
                return true;
            }
            String query = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).toLowerCase(Locale.ROOT).replace(' ', '_');
            List<String> matches = new ArrayList<>();
            for (ShopSection section : catalog.sections()) {
                if (!player.hasPermission("mirashop.section.*") && !player.hasPermission("mirashop.section." + section.id())) continue;
                for (ShopItem item : section.items()) {
                    if (!item.id().contains(query) && !item.material().name().toLowerCase(Locale.ROOT).contains(query)) continue;
                    String mode = item.canBuy() && item.canSell() ? "&eBuy/Sell" : item.canBuy() ? "&aBuy Only" : item.canSell() ? "&cSell Only" : "&7Disabled";
                    String buy = item.canBuy() ? plugin.money(item.buyPrice()) : "-";
                    String sell = item.canSell() ? plugin.money(item.sellPrice()) : "-";
                    matches.add("&d" + pretty(item.id()) + " &7[" + section.displayName() + "&7] " + mode + " &7Buy: &f" + buy + " &7Sell: &f" + sell);
                }
            }
            if (matches.isEmpty()) plugin.msg(player, "&cNo shop items matched &f" + query.replace('_', ' ') + "&c.");
            else {
                plugin.msg(player, "&dShop Search &7- &f" + matches.size() + " &7match" + (matches.size() == 1 ? "" : "es"));
                for (String line : matches.stream().limit(12).toList()) plugin.msg(player, line);
                if (matches.size() > 12) plugin.msg(player, "&7Showing first 12 matches.");
            }
            return true;
        }

        ShopSection section = catalog.section(args[0]).orElse(null);
        if (section == null) { plugin.msg(player, "&cUnknown shop section. Try &f/shop search <item>&c."); return true; }
        if (!player.hasPermission("mirashop.section.*") && !player.hasPermission("mirashop.section." + section.id())) {
            plugin.msg(player, plugin.message("no-permission")); return true;
        }
        gui.openSection(player, section);
        return true;
    }

    private static String pretty(String input) {
        StringBuilder out = new StringBuilder();
        for (String part : input.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
