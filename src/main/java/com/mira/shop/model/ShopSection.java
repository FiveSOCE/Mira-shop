package com.mira.shop.model;

import org.bukkit.Material;
import java.util.List;

public record ShopSection(String id, String displayName, Material icon, List<ShopItem> items) {}
