package com.mira.shop;

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
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class MiraShopPlugin extends JavaPlugin {
    private final DecimalFormat money = new DecimalFormat("0.00");
    private ShopCatalog catalog;
    private EconomyService economy;
    private EconomyStatsService stats;
    private SaleEventService sales;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        EconomyRebalanceMigration.apply(this);
        catalog = new ShopCatalog(this);
        catalog.load();
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

        Bukkit.getScheduler().runTask(this, this::syncEssentialsWorth);
        getLogger().info("MiraShop v" + getPluginMeta().getVersion() + " enabled with " + catalog.sections().size() + " preset sections and " + sales.active().size() + " active sale(s).");
    }

    public void reloadAll() {
        reloadConfig();
        catalog.load();
        economy.hook();
        Bukkit.getScheduler().runTask(this, this::syncEssentialsWorth);
    }

    public ShopCatalog catalog() { return catalog; }
    public EconomyStatsService stats() { return stats; }
    public SaleEventService sales() { return sales; }

    public void syncEssentialsWorth() {
        if (Bukkit.getPluginManager().getPlugin("Essentials") == null) return;
        Set<Material> synced = new HashSet<>();
        catalog.sections().forEach(section -> section.items().forEach(item -> {
            if (!item.canSell() || !synced.add(item.material())) return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setworth " + item.material().name().toLowerCase(Locale.ROOT) + " " + item.sellPrice());
        }));
    }

    public String message(String key) { return getConfig().getString("messages." + key, "&cMissing message: " + key); }
    public void msg(CommandSender sender, String message) { sender.sendMessage(Text.c(getConfig().getString("messages.prefix", "&5[MiraShop] &r") + message)); }
    public String money(double value) { String symbol = getConfig().getString("currency.symbol", "$"); return symbol + money.format(Math.max(0D, value)); }
}
