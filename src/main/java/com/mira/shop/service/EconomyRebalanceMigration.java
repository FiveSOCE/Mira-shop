package com.mira.shop.service;

import com.mira.shop.MiraShopPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyRebalanceMigration {
    private static final String MARKER = "meta.migrations.v0_1_5_economy_rebalance";

    private EconomyRebalanceMigration() {}

    public static void apply(MiraShopPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (yaml.getBoolean(MARKER, false)) return;

        Map<String, Price> prices = new LinkedHashMap<>();

        put(prices, "blocks.stone", 6, 2);
        put(prices, "blocks.cobblestone", 4, 1);
        put(prices, "blocks.deepslate", 6, 2);
        put(prices, "blocks.dirt", 4, 1);
        put(prices, "blocks.grass_block", 10, 2);
        put(prices, "blocks.sand", 6, 2);
        put(prices, "blocks.red_sand", 8, 2.5);
        put(prices, "blocks.gravel", 6, 2);
        put(prices, "blocks.glass", 10, 2);
        put(prices, "blocks.oak_log", 12, 4);
        put(prices, "blocks.spruce_log", 12, 4);
        put(prices, "blocks.birch_log", 12, 4);
        put(prices, "blocks.jungle_log", 12, 4);
        put(prices, "blocks.acacia_log", 12, 4);
        put(prices, "blocks.dark_oak_log", 12, 4);
        put(prices, "blocks.mangrove_log", 14, 4);
        put(prices, "blocks.cherry_log", 14, 4);
        put(prices, "blocks.netherrack", 4, 1);
        put(prices, "blocks.soul_sand", 12, 3);
        put(prices, "blocks.obsidian", 35, 10);

        put(prices, "farming.wheat", 12, 4);
        put(prices, "farming.wheat_seeds", 4, 1);
        put(prices, "farming.carrot", 12, 4);
        put(prices, "farming.potato", 12, 4);
        put(prices, "farming.beetroot", 10, 3);
        put(prices, "farming.beetroot_seeds", 4, 1);
        put(prices, "farming.sugar_cane", 15, 5);
        put(prices, "farming.cactus", 15, 5);
        put(prices, "farming.pumpkin", 20, 6);
        put(prices, "farming.melon_slice", 5, 1.5);
        put(prices, "farming.cocoa_beans", 12, 4);
        put(prices, "farming.bamboo", 8, 2);
        put(prices, "farming.kelp", 6, 1.5);
        put(prices, "farming.nether_wart", 20, 7);
        put(prices, "farming.sweet_berries", 10, 3);

        put(prices, "food.bread", 20, 5);
        put(prices, "food.apple", 20, 5);
        put(prices, "food.cooked_beef", 35, 8);
        put(prices, "food.cooked_porkchop", 35, 8);
        put(prices, "food.cooked_chicken", 25, 6);
        put(prices, "food.cooked_mutton", 30, 7);
        put(prices, "food.cooked_rabbit", 30, 7);
        put(prices, "food.baked_potato", 18, 4);
        put(prices, "food.pumpkin_pie", 30, 6);
        put(prices, "food.golden_carrot", 100, 20);
        put(prices, "food.golden_apple", 1500, 300);

        put(prices, "ores.coal", 20, 5);
        put(prices, "ores.raw_iron", 50, 12);
        put(prices, "ores.iron_ingot", 120, 40);
        put(prices, "ores.raw_copper", 15, 4);
        put(prices, "ores.copper_ingot", 22, 6);
        put(prices, "ores.raw_gold", 80, 20);
        put(prices, "ores.gold_ingot", 100, 30);
        put(prices, "ores.redstone", 15, 4);
        put(prices, "ores.lapis_lazuli", 18, 5);
        put(prices, "ores.quartz", 30, 8);
        put(prices, "ores.emerald", 200, 85);
        put(prices, "ores.diamond", 400, 100);
        put(prices, "ores.netherite_scrap", 2500, 600);
        put(prices, "ores.netherite_ingot", 10000, 2500);

        put(prices, "mobdrops.rotten_flesh", 15, 5);
        put(prices, "mobdrops.bone", 20, 8);
        put(prices, "mobdrops.arrow", 20, 6);
        put(prices, "mobdrops.leather", 15, 4);
        put(prices, "mobdrops.feather", 10, 3);
        put(prices, "mobdrops.string", 15, 5);
        put(prices, "mobdrops.spider_eye", 15, 5);
        put(prices, "mobdrops.gunpowder", 80, 40);
        put(prices, "mobdrops.ender_pearl", 75, 20);
        put(prices, "mobdrops.blaze_rod", 100, 50);
        put(prices, "mobdrops.ghast_tear", 300, 50);
        put(prices, "mobdrops.slime_ball", 30, 8);
        put(prices, "mobdrops.magma_cream", 40, 10);
        put(prices, "mobdrops.phantom_membrane", 100, 20);
        put(prices, "mobdrops.prismarine_shard", 25, 6);

        put(prices, "redstone.redstone", 15, 4);
        put(prices, "redstone.repeater", 60, 15);
        put(prices, "redstone.comparator", 80, 20);
        put(prices, "redstone.piston", 75, 20);
        put(prices, "redstone.sticky_piston", 110, 30);
        put(prices, "redstone.observer", 90, 20);
        put(prices, "redstone.hopper", 250, 60);
        put(prices, "redstone.dispenser", 100, 25);
        put(prices, "redstone.dropper", 70, 15);
        put(prices, "redstone.redstone_lamp", 90, 20);
        put(prices, "redstone.target", 50, 12);

        put(prices, "spawners.chicken_spawner", 50000, -1);
        put(prices, "spawners.pig_spawner", 75000, -1);
        put(prices, "spawners.cow_spawner", 100000, -1);
        put(prices, "spawners.zombie_spawner", 175000, -1);
        put(prices, "spawners.skeleton_spawner", 225000, -1);
        put(prices, "spawners.polar_bear_spawner", 350000, -1);
        put(prices, "spawners.blaze_spawner", 650000, -1);
        put(prices, "spawners.evoker_spawner", 1250000, -1);
        put(prices, "spawners.iron_golem_spawner", 3000000, -1);

        for (Map.Entry<String, Price> entry : prices.entrySet()) {
            String base = "sections." + entry.getKey();
            if (!yaml.contains(base)) continue;
            yaml.set(base + ".buy", entry.getValue().buy());
            yaml.set(base + ".sell", entry.getValue().sell());
        }

        yaml.set(MARKER, true);
        try {
            yaml.save(file);
            plugin.getLogger().info("Applied MiraShop v0.1.5 economy rebalance to existing shops.yml.");
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save v0.1.5 economy rebalance", ex);
        }
    }

    private static void put(Map<String, Price> prices, String path, double buy, double sell) {
        prices.put(path, new Price(buy, sell));
    }

    private record Price(double buy, double sell) {}
}
