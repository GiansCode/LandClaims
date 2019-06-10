package me.itsmas.landclaims.hook;

import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import com.google.inject.Inject;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.tier.Tier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class HolographHook
{
    @Inject
    private Claims plugin;

    public void init()
    {

    }

    public NamedHologram createHologram(Location location, Tier tier, Player player, String id)
    {
        List<String> lines = new ArrayList<>(tier.hologramLines);
        lines.replaceAll(line -> MessageFormat.format(line, player.getName()));

        NamedHologram hologram = new NamedHologram(location.clone().add(0.5, 1.25D + (lines.size() * 0.25D), 0.5), id);

        NamedHologramManager.addHologram(hologram);
        hologram.refreshAll();

        HologramDatabase.saveHologram(hologram);
        HologramDatabase.trySaveToDisk();

        lines.forEach(hologram::appendTextLine);

        return hologram;
    }
}
