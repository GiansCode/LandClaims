package me.itsmas.landclaims;

import com.google.inject.Singleton;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

@Singleton
public class ClaimsConfig
{
    public void init(Claims plugin)
    {
        FileConfiguration config = plugin.getConfig();

        inactivityDays = config.getInt("region_settings.inactivity_days");

        regionOutlineParticle = Particle.valueOf(config.getString("region_settings.outline.particle").toUpperCase());

        economyEnabled = config.getBoolean("economy.enabled");
        economyPrice = config.getDouble("economy.price");

        carryOldMembers = config.getBoolean("misc.carry_old_members");

        actionbarEnabled = config.getBoolean("actionbar.enabled");
    }

    public int inactivityDays;

    public Particle regionOutlineParticle;

    public boolean economyEnabled;
    public double economyPrice;

    public boolean carryOldMembers;

    public boolean actionbarEnabled;
}
