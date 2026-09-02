package com.mira.shop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AdminHolder implements InventoryHolder {
    public enum Type { SECTIONS, ITEMS, ITEM_EDIT }
    private final Type type;
    private final String section;
    private final String item;
    private Inventory inventory;

    public AdminHolder(Type type, String section, String item) {
        this.type = type; this.section = section; this.item = item;
    }
    public Type type() { return type; }
    public String section() { return section; }
    public String item() { return item; }
    public void bind(Inventory inventory) { this.inventory = inventory; }
    @Override public Inventory getInventory() { return inventory; }
}
