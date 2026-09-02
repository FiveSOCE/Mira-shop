package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public final class ShopCatalog {
    private final MiraShopPlugin plugin;
    private final Map<String, ShopSection> sections = new LinkedHashMap<>();
    private File file;
    private YamlConfiguration yaml;

    public ShopCatalog(MiraShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) plugin.saveResource("shops.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        sections.clear();
        ConfigurationSection root = yaml.getConfigurationSection("sections");
        if (root == null) return;
        for (String sectionId : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(sectionId);
            if (sec == null) continue;
            Material icon = Material.matchMaterial(sec.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            List<ShopItem> items = new ArrayList<>();
            ConfigurationSection itemRoot = sec.getConfigurationSection("items");
            if (itemRoot != null) {
                for (String itemId : itemRoot.getKeys(false)) {
                    ConfigurationSection item = itemRoot.getConfigurationSection(itemId);
                    if (item == null) continue;
                    Material material = Material.matchMaterial(item.getString("material", itemId));
                    if (material == null || material.isAir()) continue;
                    double buy = item.getDouble("buy", -1D);
                    double sell = item.getDouble("sell", -1D);
                    items.add(new ShopItem(itemId.toLowerCase(Locale.ROOT), material, buy, sell));
                }
            }
            sections.put(sectionId.toLowerCase(Locale.ROOT), new ShopSection(sectionId.toLowerCase(Locale.ROOT), sec.getString("name", sectionId), icon, List.copyOf(items)));
        }
    }

    public Collection<ShopSection> sections() { return Collections.unmodifiableCollection(sections.values()); }
    public Optional<ShopSection> section(String id) { return Optional.ofNullable(sections.get(id.toLowerCase(Locale.ROOT))); }

    public Optional<ShopItem> findByMaterial(Material material) {
        for (ShopSection section : sections.values()) {
            for (ShopItem item : section.items()) if (item.material() == material) return Optional.of(item);
        }
        return Optional.empty();
    }

    public synchronized void setPrice(String sectionId, String itemId, Double buy, Double sell) {
        String base = "sections." + sectionId + ".items." + itemId;
        if (buy != null) yaml.set(base + ".buy", buy);
        if (sell != null) yaml.set(base + ".sell", sell);
        saveAndReload();
    }

    public synchronized void addHandItem(String sectionId, String itemId, Material material, double buy, double sell) {
        String base = "sections." + sectionId + ".items." + itemId;
        yaml.set(base + ".material", material.name());
        yaml.set(base + ".buy", buy);
        yaml.set(base + ".sell", sell);
        saveAndReload();
    }

    public synchronized void removeItem(String sectionId, String itemId) {
        yaml.set("sections." + sectionId + ".items." + itemId, null);
        saveAndReload();
    }

    private void saveAndReload() {
        try { yaml.save(file); } catch (Exception ex) { throw new IllegalStateException("Failed to save shops.yml", ex); }
        load();
    }
}
