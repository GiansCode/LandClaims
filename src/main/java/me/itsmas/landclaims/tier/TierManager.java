package me.itsmas.landclaims.tier;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.util.Files;
import me.itsmas.landclaims.util.Message;
import me.itsmas.landclaims.util.UtilServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class TierManager implements Listener
{
    @Inject
    private Claims plugin;

    @Inject
    private PaperPluginLogger logger;

    public Tier getTierByName(String name)
    {
        for (Tier tier : tiers)
        {
            if (tier.toString().equals(name))
            {
                return tier;
            }
        }

        return null;
    }

    public Tier getByStack(ItemStack stack)
    {
        for (Tier tier : getTiers())
        {
            if (stack.isSimilar(tier.stack))
            {
                return tier;
            }
        }

        return null;
    }

    private final Set<Tier> tiers = new HashSet<>();

    public Set<Tier> getTiers()
    {
        return Collections.unmodifiableSet(tiers);
    }

    public void init()
    {
        UtilServer.registerListener(this);

        File file = new File(plugin.getDataFolder() + File.separator + "tiers", "1.yml");

        if (!file.exists())
        {
            plugin.saveResource("tiers" + File.separator + "1.yml", false);
        }

        loadTiers();
    }

    public void loadTiers()
    {
        tiers.clear();

        File folder = new File(plugin.getDataFolder() + File.separator + "tiers");

        if (folder.isDirectory())
        {
            File[] files = folder.listFiles();

            if (files == null)
            {
                return;
            }

            for (File file : files)
            {
                String name = Files.getFileName(file);

                try
                {
                    tiers.add(Tier.fromFile(Integer.parseInt(name), file));
                }
                catch (NumberFormatException ex)
                {
                    logger.severe("Invalid tier name: " + name);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemDrop().getItemStack();

        Tier tier = getByStack(stack);

        if (tier != null && !tier.droppable)
        {
            event.setCancelled(true);
            Message.NO_DROP_TIER.send(player);
        }
    }
}