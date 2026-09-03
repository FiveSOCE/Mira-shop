package com.mira.shop.listener;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.service.SaleEventService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public final class SaleCommandListener implements Listener {
    private final MiraShopPlugin plugin;
    private final SaleEventService sales;

    public SaleCommandListener(MiraShopPlugin plugin, SaleEventService sales) {
        this.plugin = plugin;
        this.sales = sales;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().substring(1).trim().split("\\s+");
        if (args.length < 2 || !args[0].equalsIgnoreCase("mshop") || !args[1].equalsIgnoreCase("sale")) return;
        if (!event.getPlayer().hasPermission("mirashop.admin")) return;
        event.setCancelled(true);
        if (args.length < 3) { help(event); return; }
        switch (args[2].toLowerCase(Locale.ROOT)) {
            case "list" -> list(event);
            case "stop", "end" -> {
                if (args.length < 4) { plugin.msg(event.getPlayer(), "&eUsage: /mshop sale stop <id>"); return; }
                plugin.msg(event.getPlayer(), sales.stop(args[3]) ? "&aSale stopped." : "&cSale not found.");
            }
            case "start" -> start(event, args);
            default -> help(event);
        }
    }

    private void start(PlayerCommandPreprocessEvent event, String[] args) {
        if (args.length < 7) {
            plugin.msg(event.getPlayer(), "&eUsage: /mshop sale start <id> <buyDiscount%> <sellBonus%> <minutes> [all|section:<id>|item:<id>]");
            return;
        }
        try {
            double buy = Double.parseDouble(args[4]);
            double sell = Double.parseDouble(args[5]);
            long minutes = Long.parseLong(args[6]);
            String scope = args.length >= 8 ? args[7] : "all";
            var sale = sales.start(args[3], buy, sell, minutes, scope);
            plugin.msg(event.getPlayer(), "&aStarted sale &f" + sale.id() + " &7scope &f" + sale.scope() + " &7buy discount &a" + sale.buyDiscountPercent() + "% &7sell bonus &a" + sale.sellBonusPercent() + "% &7for &f" + sale.minutesRemaining() + "m&7.");
        } catch (NumberFormatException ex) {
            plugin.msg(event.getPlayer(), "&cDiscount, bonus and minutes must be numbers.");
        }
    }

    private void list(PlayerCommandPreprocessEvent event) {
        var active = sales.active();
        plugin.msg(event.getPlayer(), "&dActive MiraShop Sales: &f" + active.size());
        if (active.isEmpty()) plugin.msg(event.getPlayer(), "&7None.");
        for (var sale : active) plugin.msg(event.getPlayer(), "&7- &f" + sale.id() + " &7" + sale.scope() + " &a-" + sale.buyDiscountPercent() + "% buy &b+" + sale.sellBonusPercent() + "% sell &7" + sale.minutesRemaining() + "m left");
    }

    private void help(PlayerCommandPreprocessEvent event) {
        plugin.msg(event.getPlayer(), "&f/mshop sale list");
        plugin.msg(event.getPlayer(), "&f/mshop sale start <id> <buyDiscount%> <sellBonus%> <minutes> [scope]");
        plugin.msg(event.getPlayer(), "&f/mshop sale stop <id>");
        plugin.msg(event.getPlayer(), "&7Scopes: all, section:<section>, item:<item-id>");
    }
}
