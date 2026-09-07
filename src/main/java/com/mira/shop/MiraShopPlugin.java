package com.mira.shop;

import com.mira.shop.api.SpawnerPriceCacheEvent;
import com.mira.shop.api.SpawnerPriceService;
import com.mira.shop.api.MaterialPriceService;
import com.mira.shop.command.AdminCommand;
import com.mira.shop.command.SellAllCommand;
import com.mira.shop.command.ShopCommand;
import com.mira.shop.gui.AdminGuiService;
import com.mira.shop.gui.SellGuiService;
import com.mira.shop.gui.ShopGuiService;
import com.mira.shop.listener.*;
import com.mira.shop.service.*;
import com.mira.shop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Map;

public final class MiraShopPlugin extends JavaPlugin {
    private final DecimalFormat money = new DecimalFormat("0.00");
    private ShopCatalog catalog;
    private EconomyService economy;
    private EconomyStatsService stats;
    private SaleEventService sales;
    private CachedSpawnerPriceService spawnerPrices;
    private CachedMaterialPriceService materialPrices;
    private BukkitTask worthSyncTask;
    private long lastWorthModified = Long.MIN_VALUE;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        EconomyRebalanceMigration.apply(this);
        catalog = new ShopCatalog(this);
        catalog.load();
        spawnerPrices = new CachedSpawnerPriceService();
        spawnerPrices.rebuild(catalog);
        getServer().getServicesManager().register(SpawnerPriceService.class, spawnerPrices, this, ServicePriority.Normal);

        materialPrices = new CachedMaterialPriceService();
        materialPrices.rebuild(catalog);
        getServer().getServicesManager().register(MaterialPriceService.class, materialPrices, this, ServicePriority.Normal);
        economy = new EconomyService();
        if (!economy.hook()) getLogger().warning("No Vault economy provider detected. Shop transactions will be unavailable until one is present.");
        stats = new EconomyStatsService(this);
        sales = new SaleEventService(this);

        TransactionService transactions = new TransactionService(this, economy);
        ShopGuiService gui = new ShopGuiService(this, catalog, transactions, economy);
        SellGuiService sellGui = new SellGuiService(this, catalog, transactions);
        AdminGuiService adminGui = new AdminGuiService(this, catalog);

        getCommand("shop").setExecutor(new ShopCommand(this, catalog, gui));
        getCommand("sellall").setExecutor(new SellAllCommand(this, catalog, transactions));
        getCommand("mshop").setExecutor(new AdminCommand(this, catalog, adminGui));
        getServer().getPluginManager().registerEvents(new ShopMenuListener(gui), this);
        getServer().getPluginManager().registerEvents(new SellGuiListener(sellGui), this);
        getServer().getPluginManager().registerEvents(new AdminMenuListener(adminGui), this);
        getServer().getPluginManager().registerEvents(new SaleCommandListener(this, sales), this);

        Bukkit.getScheduler().runTask(this, () -> {
            syncFromEssentialsWorth(true);
            publishSpawnerPriceCache();
        });
        startWorthWatcher();
        getLogger().info("MiraShop v" + getPluginMeta().getVersion() + " enabled with " + catalog.sections().size() + " preset sections and " + sales.active().size() + " active sale(s).");
    }

    public void reloadAll() {
        reloadConfig();
        catalog.load();
        economy.hook();
        materialPrices.rebuild(catalog);
        Bukkit.getScheduler().runTask(this, () -> syncFromEssentialsWorth(true));
    }

    public void publishSpawnerPriceCache() {
        if (catalog == null) return;
        if (spawnerPrices != null) {
            spawnerPrices.rebuild(catalog);
            Bukkit.getPluginManager().callEvent(new SpawnerPriceCacheEvent(spawnerPrices.buyPrices()));
        }
        if (materialPrices != null) {
            materialPrices.rebuild(catalog);
        }
    }

    public ShopCatalog catalog() { return catalog; }
    public SpawnerPriceService spawnerPrices() { return spawnerPrices; }
    public MaterialPriceService materialPrices() { return materialPrices; }
    public EconomyStatsService stats() { return stats; }
    public SaleEventService sales() { return sales; }

    private void startWorthWatcher() {
        if (worthSyncTask != null) worthSyncTask.cancel();
        long seconds = Math.max(5L, getConfig().getLong("essentials-worth.sync-seconds", 30L));
        worthSyncTask = Bukkit.getScheduler().runTaskTimer(this, () -> syncFromEssentialsWorth(false), seconds * 20L, seconds * 20L);
    }

    public void syncFromEssentialsWorth(boolean force) {
        var essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null || catalog == null) return;

        File worthFile = new File(essentials.getDataFolder(), "worth.yml");
        if (!worthFile.isFile()) {
            if (force) getLogger().warning("Essentials worth.yml not found; MiraShop sell prices were not synced.");
            return;
        }

        long modified = worthFile.lastModified();
        if (!force && modified == lastWorthModified) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(worthFile);
        ConfigurationSection section = yaml.getConfigurationSection("worth");
        if (section == null) section = yaml;

        Map<Material, Double> prices = new EnumMap<>(Material.class);
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) material = Material.matchMaterial(key.toUpperCase(java.util.Locale.ROOT));
            double value = section.getDouble(key, -1D);
            if (material != null && Double.isFinite(value) && value >= 0D) {
                prices.put(material, value);
            }
        }

        int changed = catalog.syncSellPrices(prices);
        lastWorthModified = modified;
        materialPrices.rebuild(catalog);
        if (changed > 0 || force) {
            getLogger().info("Synced " + changed + " MiraShop sell price(s) from Essentials worth.yml. "
                    + prices.size() + " worth entr" + (prices.size() == 1 ? "y" : "ies") + " loaded.");
        }
    }

    @Override
    public void onDisable() {
        if (worthSyncTask != null) {
            worthSyncTask.cancel();
            worthSyncTask = null;
        }
    }

    public String message(String key) { return getConfig().getString("messages." + key, "&cMissing message: " + key); }
    public void msg(CommandSender sender, String message) { sender.sendMessage(Text.c(getConfig().getString("messages.prefix", "&5[MiraShop] &r") + message)); }
    public String money(double value) { String symbol = getConfig().getString("currency.symbol", "$"); return symbol + money.format(Math.max(0D, value)); }
}
