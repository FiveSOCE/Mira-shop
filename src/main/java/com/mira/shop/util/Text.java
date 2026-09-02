package com.mira.shop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private Text() {}

    public static Component c(String text) {
        return LEGACY.deserialize(text == null ? "" : text)
                .decoration(TextDecoration.ITALIC, false);
    }
}
