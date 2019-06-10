package me.itsmas.landclaims.tier;

import me.itsmas.landclaims.util.Colour;
import me.itsmas.landclaims.util.Items;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class Tier
{
    public Tier(
            int tier,
            ItemStack stack,
            int radius, boolean skyToBedrock,
            boolean pvpDefault, boolean buildDefault, boolean useDefault,
            String greetingDefault, String farewellDefault,
            List<String> hologramLines,
            boolean droppable
    )
    {
        this.tier = tier;

        this.stack =  stack;

        this.radius = radius;
        this.skyToBedrock = skyToBedrock;

        this.pvpDefault = pvpDefault;
        this.buildDefault = buildDefault;
        this.useDefault = useDefault;

        this.greetingDefault = greetingDefault;
        this.farewellDefault = farewellDefault;

        this.hologramLines = Collections.unmodifiableList(hologramLines);

        this.droppable = droppable;
    }

    public final int tier;

    public final ItemStack stack;

    public final int radius;
    public final boolean skyToBedrock;

    public final boolean pvpDefault;
    public final boolean buildDefault;
    public final boolean useDefault;

    public final String greetingDefault;
    public final String farewellDefault;

    public final List<String> hologramLines;

    public final boolean droppable;

    public static Tier fromFile(int tier, File file)
    {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ItemStack stack = Items.createStack(config);

        int radius = config.getInt("settings.radius");
        boolean skyToBedrock = config.getBoolean("settings.sky_to_bedrock");

        boolean pvpDefault = config.getBoolean("flags.pvp");
        boolean buildDefault = config.getBoolean("flags.build");
        boolean useDefault = config.getBoolean("flags.use");

        String greetingDefault = config.getString("flags.greeting");
        String farewellDefault = config.getString("flags.farewell");

        List<String> hologramLines = config.getStringList("hologram");
        hologramLines.replaceAll(Colour::translate);

        boolean droppable = config.getBoolean("settings.droppable");

        return new Tier(tier, stack, radius, skyToBedrock, pvpDefault, buildDefault, useDefault, greetingDefault, farewellDefault, hologramLines, droppable);
    }

    @Override
    public String toString()
    {
        return String.valueOf(tier);
    }
}
