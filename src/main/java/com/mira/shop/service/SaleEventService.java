package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public final class SaleEventService {
    private final MiraShopPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;

    public SaleEventService(MiraShopPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "sales.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        cleanup();
    }

    public synchronized Sale start(String id, double buyDiscountPercent, double sellBonusPercent, long minutes, String scope) {
        id = sanitize(id);
        buyDiscountPercent = clamp(buyDiscountPercent, 0D, 100D);
        sellBonusPercent = clamp(sellBonusPercent, 0D, 1000D);
        long endsAt = System.currentTimeMillis() + Math.max(1L, minutes) * 60_000L;
        String base = "sales." + id;
        yaml.set(base + ".buy-discount", buyDiscountPercent);
        yaml.set(base + ".sell-bonus", sellBonusPercent);
        yaml.set(base + ".scope", normalizeScope(scope));
        yaml.set(base + ".started-at", System.currentTimeMillis());
        yaml.set(base + ".ends-at", endsAt);
        save();
        return get(id).orElseThrow();
    }

    public synchronized boolean stop(String id) {
        id = sanitize(id);
        if (!yaml.contains("sales." + id)) return false;
        yaml.set("sales." + id, null);
        save();
        return true;
    }

    public synchronized List<Sale> active() {
        cleanup();
        ConfigurationSection root = yaml.getConfigurationSection("sales");
        if (root == null) return List.of();
        List<Sale> out = new ArrayList<>();
        for (String id : root.getKeys(false)) get(id).ifPresent(out::add);
        out.sort(Comparator.comparingLong(Sale::endsAt));
        return List.copyOf(out);
    }

    public synchronized double buyPrice(ShopItem item) {
        if (item == null || !item.canBuy()) return -1D;
        double price = item.buyPrice();
        for (Sale sale : active()) if (matches(sale.scope(), item)) price *= (1D - sale.buyDiscountPercent() / 100D);
        return Math.max(0D, price);
    }

    public synchronized double sellPrice(ShopItem item) {
        if (item == null || !item.canSell()) return -1D;
        double price = item.sellPrice();
        for (Sale sale : active()) if (matches(sale.scope(), item)) price *= (1D + sale.sellBonusPercent() / 100D);
        return Math.max(0D, price);
    }

    public synchronized List<Sale> salesFor(ShopItem item) {
        return active().stream().filter(s -> matches(s.scope(), item)).toList();
    }

    private Optional<Sale> get(String id) {
        String base = "sales." + sanitize(id);
        if (!yaml.contains(base + ".ends-at")) return Optional.empty();
        return Optional.of(new Sale(sanitize(id), yaml.getDouble(base + ".buy-discount", 0D), yaml.getDouble(base + ".sell-bonus", 0D),
                yaml.getString(base + ".scope", "all"), yaml.getLong(base + ".started-at", 0L), yaml.getLong(base + ".ends-at", 0L)));
    }

    private boolean matches(String scope, ShopItem item) {
        if (scope.equals("all")) return true;
        if (scope.startsWith("item:")) return item.id().equalsIgnoreCase(scope.substring(5));
        if (scope.startsWith("section:")) {
            String sectionId = scope.substring(8);
            return plugin.catalog().section(sectionId).map(section -> section.items().stream().anyMatch(x -> x.id().equalsIgnoreCase(item.id()))).orElse(false);
        }
        return false;
    }

    private void cleanup() {
        ConfigurationSection root = yaml.getConfigurationSection("sales");
        if (root == null) return;
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (String id : new HashSet<>(root.getKeys(false))) {
            if (yaml.getLong("sales." + id + ".ends-at", 0L) <= now) { yaml.set("sales." + id, null); changed = true; }
        }
        if (changed) save();
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) return "all";
        String normalized = scope.toLowerCase(Locale.ROOT);
        if (normalized.equals("all") || normalized.startsWith("section:") || normalized.startsWith("item:")) return normalized;
        return "section:" + normalized;
    }
    private static String sanitize(String id) { return id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private void save() { try { yaml.save(file); } catch (Exception ex) { plugin.getLogger().warning("Could not save sales.yml: " + ex.getMessage()); } }

    public record Sale(String id, double buyDiscountPercent, double sellBonusPercent, String scope, long startedAt, long endsAt) {
        public long minutesRemaining() { return Math.max(0L, (endsAt - System.currentTimeMillis() + 59_999L) / 60_000L); }
    }
}
