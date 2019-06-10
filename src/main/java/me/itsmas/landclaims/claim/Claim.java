package me.itsmas.landclaims.claim;

import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.google.gson.JsonObject;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Claim
{
    private final Claims plugin;

    private OfflinePlayer owner;

    public OfflinePlayer getOwner()
    {
        return owner;
    }

    Location centre;

    boolean enterLeaveMessages;

    int tier;
    Tier _tier;

    String id;

    public ProtectedCuboidRegion region;

    Claim(Claims plugin, OfflinePlayer owner, Location centre, boolean enterLeaveMessages, int tier, String id, ProtectedCuboidRegion region)
    {
        this.plugin = plugin;

        this.owner = owner;
        this.centre = centre;
        this.enterLeaveMessages = enterLeaveMessages;
        this.tier = tier;
        this.id = id;
        this.region = region;

        this._tier = plugin.getInstance(TierManager.class).getTierByName(String.valueOf(tier));
    }

    JsonObject toJson()
    {
        JsonObject object = new JsonObject();

        object.addProperty("owner", owner.getUniqueId().toString());
        object.addProperty("centre", toString(centre));
        object.addProperty("tier", tier);
        object.addProperty("enterLeaveMessages", enterLeaveMessages);
        object.addProperty("id", id);
        object.addProperty("region", region.getId());

        return object;
    }

    private final Set<Player> outlineToggled = new HashSet<>();

    public void tryAddMember(Player executor, OfflinePlayer target)
    {
        if (executor.getUniqueId().equals(target.getUniqueId()))
        {
            Message.ADD_NO_SELF.send(executor);
            return;
        }

        if (isMember(target))
        {
            Message.MEMBER_ALREADY_ADDED.send(executor);
            return;
        }

        region.getMembers().addPlayer(target.getUniqueId());
        Message.MEMBER_ADDED.send(executor, target.getName());
    }

    public void tryRemoveMember(Player executor, OfflinePlayer target)
    {
        if (executor.getUniqueId().equals(target.getUniqueId()))
        {
            Message.REMOVE_NO_SELF.send(executor);
            return;
        }

        if (!isMember(target))
        {
            Message.MEMBER_NOT_A_MEMBER.send(executor);
            return;
        }

        region.getMembers().removePlayer(target.getUniqueId());
        Message.MEMBER_REMOVED.send(executor, target.getName());
    }

    void transferOwnership(Player executor, OfflinePlayer newOwner)
    {
        owner = newOwner;

        plugin.getInstance(ClaimManager.class).enableGreetings(region, newOwner, _tier);

        try
        {
            NamedHologram hologram = HologramDatabase.loadHologram(id);

            hologram.clearLines();

            List<String> lines = new ArrayList<>(_tier.hologramLines);
            lines.replaceAll(line -> MessageFormat.format(line, newOwner.getName()));

            lines.forEach(hologram::addLine);
        }
        catch (Exception ignored) {}

        Message.OWNERSHIP_CHANGED.send(executor, newOwner.getName());
    }

    private boolean isMember(OfflinePlayer player)
    {
        return region.getMembers().contains(player.getUniqueId());
    }

    void removeOutline(Player player)
    {
        outlineToggled.remove(player);
    }

    boolean hasOutlineToggled(Player player)
    {
        return outlineToggled.contains(player);
    }

    private Set<Location> outlineBlocks;

    void addOutline(Player player, Set<Location> blocks)
    {
        outlineBlocks = blocks;

        outlineToggled.add(player);
    }

    void showOutlineParticles(Particle particle)
    {
        if (outlineBlocks == null || outlineToggled.isEmpty())
        {
            return;
        }

        outlineBlocks.forEach(location ->
            outlineToggled.forEach(player ->
                player.spawnParticle(particle, location, 1)
            )
        );
    }

    boolean canUseMenu(Player player)
    {
        return player.getUniqueId().equals(owner.getUniqueId()) || player.hasPermission("landclaims.bypass");
    }

    static Claim fromJson(JsonObject object, WorldGuardPlugin worldGuard, Claims plugin)
    {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(object.get("owner").getAsString()));
        Location location = fromString(object.get("centre").getAsString());
        boolean enterLeaveMessages = object.get("enterLeaveMessages").getAsBoolean();
        int tier = object.get("tier").getAsInt();
        String id = object.get("id").getAsString();
        ProtectedCuboidRegion region = (ProtectedCuboidRegion) worldGuard.getRegionManager(location.getWorld()).getRegion(id);

        return new Claim(plugin, owner, location, enterLeaveMessages, tier, id, region);
    }

    private static String toString(Location location)
    {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    private static Location fromString(String string)
    {
        String[] split = string.split(";");

        World world = Bukkit.getWorld(split[0]);
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);

        return new Location(world, x, y, z);
    }
}
