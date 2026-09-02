package com.mira.shop.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyService {
    private Economy economy;

    public boolean hook() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = rsp == null ? null : rsp.getProvider();
        return economy != null;
    }

    public boolean available() { return economy != null; }
    public double balance(Player player) { return economy == null ? 0D : economy.getBalance(player); }
    public boolean withdraw(Player player, double amount) {
        return economy != null && amount >= 0D && economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    public boolean deposit(Player player, double amount) {
        return economy != null && amount >= 0D && economy.depositPlayer(player, amount).transactionSuccess();
    }
}
