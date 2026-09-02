package com.mira.shop.command;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.gui.ShopGuiService;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.ShopCatalog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        ShopSection section = catalog.section(args[0]).orElse(null);
        if (section == null) { plugin.msg(player, "&cUnknown shop section."); return true; }
        if (!player.hasPermission("mirashop.section.*") && !player.hasPermission("mirashop.section." + section.id())) {
            plugin.msg(player, plugin.message("no-permission")); return true;
        }
        gui.openSection(player, section);
        return true;
    }
}
