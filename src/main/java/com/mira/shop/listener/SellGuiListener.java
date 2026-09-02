package com.mira.shop.listener;

import com.mira.shop.gui.SellGuiService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class SellGuiListener implements Listener {
    private final SellGuiService gui;

    public SellGuiListener(SellGuiService gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().trim().toLowerCase();
        if (raw.equals("/sell") || raw.equals("/sellhand") || raw.equals("/sell hand")) {
            event.setCancelled(true);
            gui.open(event.getPlayer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellGuiService.SellHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        gui.handle(player, event.getRawSlot());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SellGuiService.SellHolder) event.setCancelled(true);
    }
}
