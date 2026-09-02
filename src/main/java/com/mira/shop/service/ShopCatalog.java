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
    private static final Set<String> REMOVED_SECTIONS = Set.of("tools", "armor", "brewing", "misc");

    private final MiraShopPlugin plugin;
    private final Map<String, ShopSection> sections = new LinkedHashMap<>();
    private File file;
    private YamlConfiguration yaml;

    public ShopCatalog(MiraShopPlugin plugin) { this.plugin = plugin; }

    public void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) plugin.saveResource("shops.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        migrateSections();
        sections.clear();
        ConfigurationSection root = yaml.getConfigurationSection("sections");
        if (root == null) return;
        for (String sectionId : root.getKeys(false)) {
            String normalized = sectionId.toLowerCase(Locale.ROOT);
            if (REMOVED_SECTIONS.contains(normalized)) continue;
            ConfigurationSection sec = root.getConfigurationSection(sectionId);
            if (sec == null) continue;
            Material icon = Material.matchMaterial(sec.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            List<ShopItem> items = new ArrayList<>();
            ConfigurationSection itemRoot = sec.getConfigurationSection("items");
            if (itemRoot != null) for (String itemId : itemRoot.getKeys(false)) {
                ConfigurationSection item = itemRoot.getConfigurationSection(itemId);
                if (item == null) continue;
                Material material = Material.matchMaterial(item.getString("material", itemId));
                if (material == null || material.isAir()) continue;
                double buy = item.getDouble("buy", -1D);
                double sell = restrictedEquipment(material) ? -1D : item.getDouble("sell", -1D);
                items.add(new ShopItem(itemId.toLowerCase(Locale.ROOT), material, buy, sell));
            }
            sections.put(normalized, new ShopSection(normalized, sec.getString("name", sectionId), icon, List.copyOf(items)));
        }
    }

    private void migrateSections() {
        boolean changed = false;
        for (String removed : REMOVED_SECTIONS) if (yaml.contains("sections." + removed)) { yaml.set("sections." + removed, null); changed = true; }
        if (!yaml.contains("sections.spawners")) {
            yaml.set("sections.spawners.name", "&5Spawners");
            yaml.set("sections.spawners.icon", "SPAWNER");
            yaml.set("sections.spawners.items.spawner.material", "SPAWNER");
            yaml.set("sections.spawners.items.spawner.buy", -1D);
            yaml.set("sections.spawners.items.spawner.sell", -1D);
            changed = true;
        }
        ConfigurationSection root = yaml.getConfigurationSection("sections");
        if (root != null) for (String sectionId : root.getKeys(false)) {
            ConfigurationSection items = root.getConfigurationSection(sectionId + ".items");
            if (items == null) continue;
            for (String itemId : items.getKeys(false)) {
                String base = "sections." + sectionId + ".items." + itemId;
                Material material = Material.matchMaterial(yaml.getString(base + ".material", itemId));
                if (material != null && restrictedEquipment(material) && yaml.getDouble(base + ".sell", -1D) >= 0D) {
                    yaml.set(base + ".sell", -1D);
                    changed = true;
                }
            }
        }
        if (changed) saveYaml();
    }

    public Collection<ShopSection> sections() { return Collections.unmodifiableCollection(sections.values()); }
    public Optional<ShopSection> section(String id) { return Optional.ofNullable(sections.get(id.toLowerCase(Locale.ROOT))); }
    public Optional<ShopItem> findByMaterial(Material material) { for (ShopSection section : sections.values()) for (ShopItem item : section.items()) if (item.material() == material) return Optional.of(item); return Optional.empty(); }

    public synchronized int setPriceByMaterial(Material material, double price) {
        ConfigurationSection root = yaml.getConfigurationSection("sections");
        if (root == null) return 0;
        int changed = 0;
        for (String sectionId : root.getKeys(false)) {
            if (REMOVED_SECTIONS.contains(sectionId.toLowerCase(Locale.ROOT))) continue;
            ConfigurationSection itemRoot = root.getConfigurationSection(sectionId + ".items");
            if (itemRoot == null) continue;
            for (String itemId : itemRoot.getKeys(false)) {
                String base = "sections." + sectionId + ".items." + itemId;
                Material configured = Material.matchMaterial(yaml.getString(base + ".material", itemId));
                if (configured != material) continue;
                yaml.set(base + ".buy", price);
                yaml.set(base + ".sell", restrictedEquipment(material) ? -1D : price);
                changed++;
            }
        }
        if (changed > 0) saveAndReload();
        return changed;
    }

    public synchronized void setPrice(String sectionId, String itemId, Double buy, Double sell) {
        String base = "sections." + sectionId + ".items." + itemId;
        Material material = Material.matchMaterial(yaml.getString(base + ".material", itemId));
        if (buy != null) yaml.set(base + ".buy", buy);
        if (sell != null) yaml.set(base + ".sell", material != null && restrictedEquipment(material) ? -1D : sell);
        saveAndReload();
    }

    public synchronized void addHandItem(String sectionId, String itemId, Material material, double buy, double sell) {
        String base = "sections." + sectionId + ".items." + itemId;
        yaml.set(base + ".material", material.name());
        yaml.set(base + ".buy", buy);
        yaml.set(base + ".sell", restrictedEquipment(material) ? -1D : sell);
        saveAndReload();
    }

    public synchronized void removeItem(String sectionId, String itemId) { yaml.set("sections." + sectionId + ".items." + itemId, null); saveAndReload(); }

    public static boolean restrictedEquipment(Material material) {
        String n = material.name();
        return n.endsWith("_SWORD") || n.endsWith("_PICKAXE") || n.endsWith("_AXE") || n.endsWith("_SHOVEL") || n.endsWith("_HOE")
                || n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS")
                || material == Material.BOW || material == Material.CROSSBOW || material == Material.TRIDENT || material == Material.MACE
                || material == Material.SHIELD || material == Material.ELYTRA || material == Material.FISHING_ROD || material == Material.SHEARS
                || material == Material.FLINT_AND_STEEL || material == Material.BRUSH;
    }

    private void saveYaml() { try { yaml.save(file); } catch (Exception ex) { throw new IllegalStateException("Failed to save shops.yml", ex); } }
    private void saveAndReload() { saveYaml(); load(); }
}
