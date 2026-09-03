package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import com.mira.shop.model.ShopItem;
import com.mira.shop.model.ShopSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public final class ShopCatalog {
    private static final Set<String> REMOVED_SECTIONS = Set.of("tools", "armor", "brewing", "misc");
    private static final NamespacedKey MIRA_SPAWNER_TYPE = NamespacedKey.fromString("miraspawners:spawner_mob_type");
    private static final String V014_MIGRATION = "meta.migrations.v0_1_4_custom_items";

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
                ItemStack template = readTemplate(item, itemId);
                if (template == null || template.getType().isAir()) continue;
                double buy = item.getDouble("buy", -1D);
                double sell = restrictedEquipment(template.getType()) ? -1D : item.getDouble("sell", -1D);
                items.add(new ShopItem(itemId.toLowerCase(Locale.ROOT), template, buy, sell));
            }
            sections.put(normalized, new ShopSection(normalized, sec.getString("name", sectionId), icon, List.copyOf(items)));
        }
    }

    private ItemStack readTemplate(ConfigurationSection item, String itemId) {
        ItemStack stored = item.getItemStack("item");
        if (stored != null) {
            stored = stored.clone();
            stored.setAmount(1);
            return stored;
        }

        String spawnerType = item.getString("spawner-type");
        if (spawnerType != null && !spawnerType.isBlank()) {
            try {
                EntityType type = EntityType.valueOf(spawnerType.trim().toUpperCase(Locale.ROOT));
                ItemStack spawner = new ItemStack(Material.SPAWNER);
                var meta = spawner.getItemMeta();
                if (MIRA_SPAWNER_TYPE != null) meta.getPersistentDataContainer().set(MIRA_SPAWNER_TYPE, PersistentDataType.STRING, type.name());
                meta.displayName(Component.text(pretty(type.name()) + " Spawner", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                spawner.setItemMeta(meta);
                return spawner;
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid spawner type in shops.yml for " + itemId + ": " + spawnerType);
            }
        }

        Material material = Material.matchMaterial(item.getString("material", itemId));
        return material == null || material.isAir() ? null : new ItemStack(material);
    }

    private void migrateSections() {
        boolean changed = false;
        for (String removed : REMOVED_SECTIONS) if (yaml.contains("sections." + removed)) { yaml.set("sections." + removed, null); changed = true; }
        if (!yaml.contains("sections.spawners")) {
            yaml.set("sections.spawners.name", "&5Spawners");
            yaml.set("sections.spawners.icon", "SPAWNER");
            changed = true;
        }

        if (!yaml.getBoolean(V014_MIGRATION, false)) {
            applyV014Presets();
            yaml.set(V014_MIGRATION, true);
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

    private void applyV014Presets() {
        yaml.set("sections.spawners.name", "&5Spawners");
        yaml.set("sections.spawners.icon", "SPAWNER");
        yaml.set("sections.spawners.items.spawner", null);

        setSpawnerPreset("chicken_spawner", "CHICKEN", 5000D);
        setSpawnerPreset("pig_spawner", "PIG", 20000D);
        setSpawnerPreset("cow_spawner", "COW", 20000D);
        setSpawnerPreset("zombie_spawner", "ZOMBIE", 150000D);
        setSpawnerPreset("skeleton_spawner", "SKELETON", 200000D);
        setSpawnerPreset("polar_bear_spawner", "POLAR_BEAR", 400000D);
        setSpawnerPreset("blaze_spawner", "BLAZE", 550000D);
        setSpawnerPreset("evoker_spawner", "EVOKER", 750000D);
        setSpawnerPreset("iron_golem_spawner", "IRON_GOLEM", 1250000D);

        setSellPreset("mobdrops", "blaze_rod", "BLAZE_ROD", 65D);
        setSellPreset("mobdrops", "rotten_flesh", "ROTTEN_FLESH", 10D);
        setSellPreset("mobdrops", "arrow", "ARROW", 15D);
        setSellPreset("mobdrops", "gunpowder", "GUNPOWDER", 80D);
        setSellPreset("mobdrops", "bone", "BONE", 12D);
        setSellPreset("mobdrops", "leather", "LEATHER", 4.5D);
        setSellPreset("mobdrops", "feather", "FEATHER", 3.5D);
        setSellPreset("ores", "iron_ingot", "IRON_INGOT", 85D);
        setSellPreset("ores", "emerald", "EMERALD", 100D);
    }

    private void setSpawnerPreset(String id, String type, double buy) {
        String base = "sections.spawners.items." + id;
        yaml.set(base + ".material", "SPAWNER");
        yaml.set(base + ".spawner-type", type);
        yaml.set(base + ".buy", buy);
        yaml.set(base + ".sell", -1D);
    }

    private void setSellPreset(String section, String id, String material, double sell) {
        String base = "sections." + section + ".items." + id;
        if (!yaml.contains(base + ".material")) yaml.set(base + ".material", material);
        if (!yaml.contains(base + ".buy")) yaml.set(base + ".buy", -1D);
        yaml.set(base + ".sell", sell);
    }

    public Collection<ShopSection> sections() { return Collections.unmodifiableCollection(sections.values()); }
    public Optional<ShopSection> section(String id) { return Optional.ofNullable(sections.get(id.toLowerCase(Locale.ROOT))); }

    public Optional<ShopItem> findByStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return Optional.empty();
        for (ShopSection section : sections.values()) {
            for (ShopItem item : section.items()) {
                if (matches(stack, item)) return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<ShopItem> findByMaterial(Material material) {
        for (ShopSection section : sections.values()) for (ShopItem item : section.items()) if (!item.customTemplate() && item.material() == material) return Optional.of(item);
        return Optional.empty();
    }

    public boolean matches(ItemStack stack, ShopItem item) {
        if (stack == null || item == null || stack.getType() != item.material()) return false;
        if (item.customTemplate()) {
            ItemStack one = stack.clone();
            one.setAmount(1);
            return one.isSimilar(item.template());
        }
        return true;
    }

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
                ItemStack stored = yaml.getItemStack(base + ".item");
                if (stored != null && stored.hasItemMeta()) continue;
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
        ItemStack stored = yaml.getItemStack(base + ".item");
        Material material = stored != null ? stored.getType() : Material.matchMaterial(yaml.getString(base + ".material", itemId));
        if (buy != null) yaml.set(base + ".buy", buy);
        if (sell != null) yaml.set(base + ".sell", material != null && restrictedEquipment(material) ? -1D : sell);
        saveAndReload();
    }

    public synchronized String addHandItem(String sectionId, ItemStack hand, double buy, double sell) {
        ItemStack template = hand.clone();
        template.setAmount(1);
        String itemId = suggestId(template);
        String base = "sections." + sectionId + ".items." + itemId;
        int suffix = 2;
        while (yaml.contains(base)) {
            itemId = suggestId(template) + "_" + suffix++;
            base = "sections." + sectionId + ".items." + itemId;
        }
        yaml.set(base + ".material", template.getType().name());
        yaml.set(base + ".item", template);
        String typedSpawner = spawnerType(template);
        if (typedSpawner != null) yaml.set(base + ".spawner-type", typedSpawner);
        yaml.set(base + ".buy", buy);
        yaml.set(base + ".sell", restrictedEquipment(template.getType()) ? -1D : sell);
        saveAndReload();
        return itemId;
    }

    public String spawnerType(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta() || MIRA_SPAWNER_TYPE == null) return null;
        return item.getItemMeta().getPersistentDataContainer().get(MIRA_SPAWNER_TYPE, PersistentDataType.STRING);
    }

    private String suggestId(ItemStack item) {
        String spawner = spawnerType(item);
        if (spawner != null) return spawner.toLowerCase(Locale.ROOT) + "_spawner";
        return item.getType().name().toLowerCase(Locale.ROOT);
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

    private static String pretty(String input) {
        StringBuilder out = new StringBuilder();
        for (String part : input.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private void saveYaml() { try { yaml.save(file); } catch (Exception ex) { throw new IllegalStateException("Failed to save shops.yml", ex); } }
    private void saveAndReload() { saveYaml(); load(); }
}
