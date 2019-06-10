package me.itsmas.landclaims.hook;

import com.google.inject.Inject;
import me.itsmas.landclaims.Claims;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PermissionsHook
{
    @Inject
    private Claims plugin;

    public void init()
    {
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("permissions_settings.enabled");

        Map<String, Integer> tempMap = new HashMap<>();

        if (enabled)
        {
            ConfigurationSection section = config.getConfigurationSection("permissions_settings.permissions");

            for (String key : section.getKeys(false))
            {
                tempMap.put(key.replace("_", "."), section.getInt(key));
            }
        }

        tempMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(
                        entry -> permissionsMap.put(entry.getKey(), entry.getValue())
                );
    }

    private boolean enabled;
    private NavigableMap<String, Integer> permissionsMap = new TreeMap<>();

    public int getMaxClaims(Player player)
    {
        if (!enabled)
        {
            return Integer.MAX_VALUE;
        }

        for (Map.Entry<String, Integer> entry : permissionsMap.entrySet())
        {
            if (player.hasPermission(entry.getKey()))
            {
                return entry.getValue();
            }
        }

        return 0;
    }
}
