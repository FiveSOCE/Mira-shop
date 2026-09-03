package com.mira.shop.command;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.gui.AdminGuiService;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import com.mira.shop.service.EconomyStatsService;
import com.mira.shop.service.ShopCatalog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AdminCommand implements CommandExecutor {
    private final MiraShopPlugin plugin;
    private final ShopCatalog catalog;
    private final AdminGuiService adminGui;

    public AdminCommand(MiraShopPlugin plugin, ShopCatalog catalog, AdminGuiService adminGui) {
        this.plugin = plugin; this.catalog = catalog; this.adminGui = adminGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mirashop.admin")) { plugin.msg(sender, plugin.message("no-permission")); return true; }
        if (args.length == 0) {
            if (sender instanceof Player player) adminGui.openSections(player);
            else printHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("edit")) {
            if (sender instanceof Player player) adminGui.openSections(player);
            else plugin.msg(sender, "&cThe GUI editor is player-only.");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            plugin.msg(sender, plugin.message("reloaded"));
            return true;
        }
        if (args[0].equalsIgnoreCase("stats")) {
            showStats(sender, args.length >= 2 ? args[1] : "24h");
            return true;
        }
        if (args[0].equalsIgnoreCase("eco")) {
            showSpawnerEconomy(sender);
            return true;
        }
        try {
            if (args[0].equalsIgnoreCase("setprice") && args.length == 2 && sender instanceof Player player) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    plugin.msg(sender, "&cHold the shop item you want to price first.");
                    return true;
                }
                double value = Double.parseDouble(args[1]);
                if (!Double.isFinite(value) || value < 0D) throw new IllegalArgumentException();

                ShopItem exact = catalog.findByStack(hand).orElse(null);
                if (exact != null && exact.customTemplate()) {
                    String sectionId = catalog.sections().stream()
                            .filter(section -> section.items().stream().anyMatch(item -> item.id().equals(exact.id())))
                            .map(section -> section.id()).findFirst().orElse(null);
                    if (sectionId == null) { plugin.msg(sender, "&cThat custom item is not configured."); return true; }
                    catalog.setPrice(sectionId, exact.id(), value, value);
                    plugin.msg(sender, "&aSet custom shop entry &f" + exact.id() + "&a to &f" + plugin.money(value) + "&a.");
                    return true;
                }

                Material material = hand.getType();
                int changed = catalog.setPriceByMaterial(material, value);
                if (changed == 0) {
                    plugin.msg(sender, "&c" + pretty(material.name()) + " is not currently configured in Mira-Shop.");
                    return true;
                }

                boolean essentialsUpdated = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setworth " + material.name().toLowerCase(Locale.ROOT) + " " + value);
                plugin.msg(sender, "&aSet &f" + pretty(material.name()) + "&a to &f" + plugin.money(value)
                        + "&a in Mira-Shop" + (essentialsUpdated ? " and Essentials worth." : ". &eEssentials worth could not be updated."));
                return true;
            }
            if (args[0].equalsIgnoreCase("setprice") && args.length >= 5) {
                String section = args[1].toLowerCase(Locale.ROOT);
                String item = args[2].toLowerCase(Locale.ROOT);
                double value = Double.parseDouble(args[4]);
                if (!Double.isFinite(value) || value < -1D) throw new IllegalArgumentException();
                if (args[3].equalsIgnoreCase("buy")) catalog.setPrice(section, item, value, null);
                else if (args[3].equalsIgnoreCase("sell")) catalog.setPrice(section, item, null, value);
                else throw new IllegalArgumentException();
                plugin.syncEssentialsWorth();
                plugin.msg(sender, "&aPrice updated.");
                return true;
            }
            if (args[0].equalsIgnoreCase("addhand") && args.length >= 5 && sender instanceof Player player) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) { plugin.msg(sender, "&cHold an item first."); return true; }
                String section = args[1].toLowerCase(Locale.ROOT);
                double buy = Double.parseDouble(args[3]);
                double sell = Double.parseDouble(args[4]);
                String generated = catalog.addHandItem(section, hand, buy, sell);
                plugin.syncEssentialsWorth();
                plugin.msg(sender, "&aExact held item added as &f" + generated + "&a.");
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
        printHelp(sender);
        return true;
    }

    private void showStats(CommandSender sender, String range) {
        java.util.List<EconomyStatsService.ItemStats> stats;
        String label;
        if (range.equalsIgnoreCase("all")) { stats = plugin.stats().allTime(); label = "All Time"; }
        else if (range.equalsIgnoreCase("7d")) { stats = plugin.stats().report(24 * 7); label = "Last 7 Days"; }
        else { stats = plugin.stats().report(24); label = "Last 24 Hours"; }

        plugin.msg(sender, "&5&m--------------------------------");
        plugin.msg(sender, "&dMiraShop Economy Stats &7- &f" + label);
        double created = stats.stream().mapToDouble(EconomyStatsService.ItemStats::moneyCreated).sum();
        double spent = stats.stream().mapToDouble(EconomyStatsService.ItemStats::moneySpent).sum();
        plugin.msg(sender, "&7Money created by sales: &a" + plugin.money(created));
        plugin.msg(sender, "&7Money removed by purchases: &c" + plugin.money(spent));
        plugin.msg(sender, "&7Net shop injection: &f" + (created >= spent ? "+" : "-") + plugin.money(Math.abs(created - spent)));
        plugin.msg(sender, "&7Top money-generating items:");
        if (stats.isEmpty()) plugin.msg(sender, "&8No transactions recorded yet.");
        int shown = 0;
        for (EconomyStatsService.ItemStats stat : stats) {
            if (stat.moneyCreated() <= 0D) continue;
            plugin.msg(sender, "&f" + (++shown) + ". &d" + pretty(stat.itemId()) + " &7Sold &f" + stat.unitsSold()
                    + " &7Created &a" + plugin.money(stat.moneyCreated()) + " &7Net &f"
                    + (stat.netInjection() >= 0D ? "+" : "-") + plugin.money(Math.abs(stat.netInjection())));
            if (shown >= 10) break;
        }
        plugin.msg(sender, "&7Ranges: &f/mshop stats 24h&7, &f7d&7, &fall");
    }

    private void showSpawnerEconomy(CommandSender sender) {
        ShopSection spawners = catalog.section("spawners").orElse(null);
        if (spawners == null) { plugin.msg(sender, "&cSpawner section is missing."); return; }
        double killsPerHour = Math.max(1D, plugin.getConfig().getDouble("eco.estimated-kills-per-hour-per-spawner", 144D));
        Map<String, Double> yield = primarySellYieldPerKill();
        plugin.msg(sender, "&5&m--------------------------------");
        plugin.msg(sender, "&dSpawner ROI Estimate &7(&f" + String.format(Locale.US, "%.0f", killsPerHour) + " kills/h per spawner&7)");
        plugin.msg(sender, "&8Estimates use primary configured sell drops only, no Looting or secondary loot.");
        for (ShopItem item : spawners.items()) {
            if (!item.canBuy()) continue;
            String type = catalog.spawnerType(item.template());
            if (type == null) type = item.id().replace("_spawner", "").toUpperCase(Locale.ROOT);
            double perKill = yield.getOrDefault(type, 0D);
            double hourly = perKill * killsPerHour;
            String roi = hourly > 0D ? String.format(Locale.US, "%.1fh", item.buyPrice() / hourly) : "N/A";
            plugin.msg(sender, "&d" + pretty(item.id()) + " &7Cost &f" + plugin.money(item.buyPrice())
                    + " &7Est/h &a" + plugin.money(hourly) + " &7ROI &f" + roi);
        }
        plugin.msg(sender, "&7Tune estimator: &feco.estimated-kills-per-hour-per-spawner&7 in config.yml.");
    }

    private Map<String, Double> primarySellYieldPerKill() {
        Map<String, Double> out = new LinkedHashMap<>();
        out.put("CHICKEN", sell("feather", 1D));
        out.put("PIG", 0D);
        out.put("COW", sell("leather", 1D));
        out.put("ZOMBIE", sell("rotten_flesh", 1D));
        out.put("SKELETON", sell("bone", 1D) + sell("arrow", 1D));
        out.put("POLAR_BEAR", 0D);
        out.put("BLAZE", sell("blaze_rod", 0.5D));
        out.put("EVOKER", sell("emerald", 0.5D));
        out.put("IRON_GOLEM", sell("iron_ingot", 4D));
        return out;
    }

    private double sell(String id, double averageAmount) {
        for (ShopSection section : catalog.sections()) for (ShopItem item : section.items()) {
            if (item.id().equalsIgnoreCase(id) && item.canSell()) return item.sellPrice() * averageAmount;
        }
        return 0D;
    }

    private void printHelp(CommandSender sender) {
        plugin.msg(sender, "&e/mshop edit");
        plugin.msg(sender, "&e/mshop reload");
        plugin.msg(sender, "&e/mshop stats <24h|7d|all> &7- economy injection report");
        plugin.msg(sender, "&e/mshop eco &7- spawner ROI estimate");
        plugin.msg(sender, "&e/mshop setprice <price> &7(hold the exact item)");
        plugin.msg(sender, "&e/mshop setprice <section> <item> <buy|sell> <price|-1>");
        plugin.msg(sender, "&e/mshop addhand <section> <id> <buy> <sell> &7(id retained for compatibility; exact item is preserved)");
        plugin.msg(sender, "&e/mshop remove <section> <item>");
    }

    private static String pretty(String input) {
        StringBuilder out = new StringBuilder();
        for (String part : input.toLowerCase(Locale.ROOT).split("_")) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
