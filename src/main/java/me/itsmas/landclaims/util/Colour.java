package me.itsmas.landclaims.util;

import org.bukkit.ChatColor;

public final class Colour
{
    private Colour() {}

    public static String translate(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
