package me.itsmas.landclaims.util;

import com.google.inject.Inject;
import me.itsmas.landclaims.Claims;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class UtilServer
{
    private UtilServer() {}

    @Inject
    private static Claims plugin;

    public static Claims getPlugin()
    {
        return plugin;
    }

    public static void registerListener(Listener listener)
    {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
