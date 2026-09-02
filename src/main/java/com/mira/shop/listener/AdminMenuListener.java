package com.mira.shop.listener;

import com.mira.shop.gui.AdminGuiService;
import com.mira.shop.gui.AdminHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class AdminMenuListener implements Listener {
    private final AdminGuiService gui;

    public AdminMenuListener(AdminGuiService gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof AdminHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        gui.handle(player, holder, event.getRawSlot());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof AdminHolder) event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!gui.awaiting(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        String message = event.getMessage();
        event.getPlayer().getServer().getScheduler().runTask(event.getPlayer().getServer().getPluginManager().getPlugin("MiraShop"), () -> gui.acceptChat(event.getPlayer(), message));
    }
}
