package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class EconomyStatsService {
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH").withZone(ZoneId.systemDefault());
    private final MiraShopPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;

    public EconomyStatsService(MiraShopPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "economy-stats.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        prune();
        save();
    }

    public synchronized void recordBuy(ShopItem item, int amount, double total) {
        record("buy", item.id(), amount, total);
    }

    public synchronized void recordSell(ShopItem item, int amount, double total) {
        record("sell", item.id(), amount, total);
    }

    private void record(String side, String item, int amount, double total) {
        String hour = HOUR.format(Instant.now());
        String base = "hours." + hour + "." + item + "." + side;
        yaml.set(base + ".units", yaml.getLong(base + ".units", 0L) + amount);
        yaml.set(base + ".money", yaml.getDouble(base + ".money", 0D) + total);
        String all = "all-time." + item + "." + side;
        yaml.set(all + ".units", yaml.getLong(all + ".units", 0L) + amount);
        yaml.set(all + ".money", yaml.getDouble(all + ".money", 0D) + total);
        save();
    }

    public synchronized List<ItemStats> report(int hours) {
        Map<String, MutableStats> totals = new HashMap<>();
        Instant cutoff = Instant.now().minusSeconds(Math.max(1, hours) * 3600L);
        ConfigurationSection root = yaml.getConfigurationSection("hours");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                Instant instant;
                try { instant = java.time.LocalDateTime.parse(key, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")).atZone(ZoneId.systemDefault()).toInstant(); }
                catch (Exception ignored) { continue; }
                if (instant.isBefore(cutoff.minusSeconds(3600))) continue;
                ConfigurationSection hour = root.getConfigurationSection(key);
                if (hour == null) continue;
                for (String item : hour.getKeys(false)) {
                    MutableStats s = totals.computeIfAbsent(item, k -> new MutableStats());
                    s.bought += hour.getLong(item + ".buy.units", 0L);
                    s.spent += hour.getDouble(item + ".buy.money", 0D);
                    s.sold += hour.getLong(item + ".sell.units", 0L);
                    s.created += hour.getDouble(item + ".sell.money", 0D);
                }
            }
        }
        return totals.entrySet().stream()
                .map(e -> new ItemStats(e.getKey(), e.getValue().bought, e.getValue().spent, e.getValue().sold, e.getValue().created))
                .sorted(Comparator.comparingDouble(ItemStats::moneyCreated).reversed())
                .toList();
    }

    public synchronized List<ItemStats> allTime() {
        List<ItemStats> out = new ArrayList<>();
        ConfigurationSection root = yaml.getConfigurationSection("all-time");
        if (root == null) return out;
        for (String item : root.getKeys(false)) {
            out.add(new ItemStats(item,
                    root.getLong(item + ".buy.units", 0L),
                    root.getDouble(item + ".buy.money", 0D),
                    root.getLong(item + ".sell.units", 0L),
                    root.getDouble(item + ".sell.money", 0D)));
        }
        out.sort(Comparator.comparingDouble(ItemStats::moneyCreated).reversed());
        return out;
    }

    public synchronized void prune() {
        ConfigurationSection root = yaml.getConfigurationSection("hours");
        if (root == null) return;
        Instant cutoff = Instant.now().minusSeconds(15L * 24L * 3600L);
        for (String key : new HashSet<>(root.getKeys(false))) {
            try {
                Instant instant = java.time.LocalDateTime.parse(key, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")).atZone(ZoneId.systemDefault()).toInstant();
                if (instant.isBefore(cutoff)) yaml.set("hours." + key, null);
            } catch (Exception ignored) { }
        }
    }

    private void save() {
        try { yaml.save(file); }
        catch (Exception ex) { plugin.getLogger().warning("Could not save economy-stats.yml: " + ex.getMessage()); }
    }

    private static final class MutableStats { long bought, sold; double spent, created; }

    public record ItemStats(String itemId, long unitsBought, double moneySpent, long unitsSold, double moneyCreated) {
        public double netInjection() { return moneyCreated - moneySpent; }
    }
}
