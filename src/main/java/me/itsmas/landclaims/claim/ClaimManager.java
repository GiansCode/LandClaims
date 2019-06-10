package me.itsmas.landclaims.claim;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.ClaimsConfig;
import me.itsmas.landclaims.hook.EconomyHook;
import me.itsmas.landclaims.hook.HolographHook;
import me.itsmas.landclaims.hook.PermissionsHook;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Colour;
import me.itsmas.landclaims.util.Items;
import me.itsmas.landclaims.util.Json;
import me.itsmas.landclaims.util.Message;
import me.itsmas.landclaims.util.UtilServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ClaimManager implements Listener
{
    @Inject
    private Claims plugin;

    @Inject
    private ClaimsConfig config;

    @Inject
    private PaperPluginLogger logger;

    @Inject
    private TierManager tierManager;

    @Inject
    private WorldGuardPlugin worldGuard;

    @Inject
    private PermissionsHook permissionsHook;

    @Inject
    private HolographHook holographHook;

    @Inject
    private EconomyHook economyHook;

    public void init()
    {
        plugin.inject(new ClaimListener());

        UtilServer.registerListener(this);

        loadClaims();
    }

    private Set<Claim> claims = new HashSet<>();
    private File claimFile;

    private void loadClaims()
    {
        claimFile = new File(plugin.getDataFolder(), "claims.json");

        if (!claimFile.exists())
        {
            try
            {
                claimFile.createNewFile();

                save();
            }
            catch (IOException ex)
            {
                logger.severe("Unable to create claims.json file");
            }
        }
        else
        {
            JsonObject json = Json.fromFile(claimFile);
            JsonArray array = json.get("claims").getAsJsonArray();

            for (JsonElement claimElement : array)
            {
                JsonObject claimObj = claimElement.getAsJsonObject();

                Claim claim = Claim.fromJson(claimObj, worldGuard, plugin);
                claims.add(claim);
            }
        }

        startParticleTimer();
    }

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save()
    {
        JsonArray claimArray = new JsonArray();
        claims.forEach(claim -> claimArray.add(claim.toJson()));

        JsonObject object = new JsonObject();
        object.add("claims", claimArray);

        try (FileWriter writer = new FileWriter(claimFile))
        {
            writer.write(gson.toJson(object));

            writer.flush();
        }
        catch (IOException ex)
        {
            logger.severe("Unable to save to claims.json file");
            ex.printStackTrace();
        }
    }

    Claim getClaimFromCentre(Block block)
    {
        for (Claim claim : claims)
        {
            if (claim.centre.equals(block.getLocation()))
            {
                return claim;
            }
        }

        return null;
    }

    Claim getClaimFromLocation(Location location)
    {
        for (Claim claim : claims)
        {
            if (claim.region.contains(locToVector(location)))
            {
                return claim;
            }
        }

        return null;
    }

    private Vector locToVector(Location location)
    {
        return new Vector(location.getX(), location.getY(), location.getZ());
    }

    boolean canCreateClaim(Player player, Location location)
    {
        if (getClaimCount(player) >= permissionsHook.getMaxClaims(player))
        {
            Message.MAX_CLAIMS_EXCEEDED.send(player);
            return false;
        }

        if (worldGuard.getRegionManager(location.getWorld()).getApplicableRegions(location).size() != 0)
        {
            Message.INSIDE_EXISTING.send(player);
            return false;
        }

        if (!economyHook.takeMoney(player))
        {
            return false;
        }

        return true;
    }

    boolean isRegionCentre(Block block)
    {
        for (Claim claim : claims)
        {
            if (claim.centre.equals(block.getLocation()))
            {
                return true;
            }
        }

        return false;
    }

    ProtectedRegion createRegion(Player player, Location location, Tier tier)
    {
        String id = "landclaims_" + player.getUniqueId() + "_" + (getClaimCount(player) + 1);

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                id,
                getMinimumPoint(location, tier),
                getMaximumPoint(location, tier)
        );

        if (region.getIntersectingRegions(worldGuard.getRegionManager(location.getWorld()).getRegions().values()).size() != 0)
        {
            // Region intersects others
            Message.CLAIM_OVERLAP.send(player);
            return null;
        }

        setOwner(region, player);
        setDefaultFlags(region);

        region.setFlag(new IntegerFlag("claim-tier"), tier.tier);

        holographHook.createHologram(location, tier, player, id);

        try
        {
            region.setParent(worldGuard.getRegionManager(location.getWorld()).getRegion("__global__"));
        }
        catch (ProtectedRegion.CircularInheritanceException ignored) {}

        worldGuard.getRegionManager(location.getWorld()).addRegion(region);

        claims.add(new Claim(plugin, player, location, true, tier.tier, id, region));
        Message.CLAIM_CREATED.send(player, tier);

        // Add previous claim members
        if (config.carryOldMembers)
        {
            claims.stream()
                .filter(claim -> claim.getOwner().getUniqueId().equals(player.getUniqueId()))
                .flatMap(claim -> claim.region.getMembers().getUniqueIds().stream())
                .distinct()
                .forEach(uuid -> region.getMembers().addPlayer(uuid));
        }

        return region;
    }

    void sendClaimEnterMessage(Player player, Claim claim)
    {
        sendMessage(player, claim, MessageFormat.format(claim._tier.greetingDefault, claim.getOwner().getName()));
    }

    void sendClaimLeaveMessage(Player player, Claim claim)
    {
        sendMessage(player, claim, MessageFormat.format(claim._tier.farewellDefault, claim.getOwner().getName()));
    }

    private void sendMessage(Player player, Claim claim, String message)
    {
        message = Colour.translate(message);

        if (!claim.enterLeaveMessages)
        {
            return;
        }

        if (config.actionbarEnabled)
        {
            player.sendActionBar(message);
        }
        else
        {
            player.sendMessage(message);
        }
    }

    void deleteClaim(Player player, Claim claim)
    {
        claims.remove(claim);

        worldGuard.getRegionManager(claim.centre.getWorld()).removeRegion(claim.region.getId());

        String id = claim.id;

        deleteHologram(id);

        claim.centre.getBlock().setType(Material.AIR);

        Items.giveItem(player, claim._tier.stack.clone());

        Message.CLAIM_DELETED.send(player);
    }

    private void deleteHologram(String id)
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holo delete " + id);
    }

    private void startParticleTimer()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                claims.forEach(claim -> claim.showOutlineParticles(config.regionOutlineParticle));
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 10L);
    }

    void toggleOutline(Player player, Claim claim)
    {
        Set<Location> locations = getOutlineLocations(claim);

        if (claim.hasOutlineToggled(player))
        {
            claim.removeOutline(player);
            Message.OUTLINE_DISABLED.send(player);
        }
        else
        {
            claim.addOutline(player, locations);
            Message.OUTLINE_ENABLED.send(player);
        }
    }

    public void togglePvp(Player executor, Claim claim)
    {
        if (worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.PVP))
        {
            Message.FLAG_PVP_DISABLED.send(executor);
            claim.region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        }
        else
        {
            Message.FLAG_PVP_ENABLED.send(executor);
            claim.region.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
        }
    }

    public boolean pvpToggled(Player executor, Claim claim)
    {
        return worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.PVP);
    }

    public void toggleBuild(Player executor, Claim claim)
    {
        if (worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.BUILD))
        {
            Message.FLAG_BUILD_DISABLED.send(executor);
            claim.region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
        }
        else
        {
            Message.FLAG_BUILD_ENABLED.send(executor);

            claim.region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);
        }
    }

    public boolean buildToggled(Player executor, Claim claim)
    {
        return worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.BUILD);
    }

    public void toggleMessages(Player executor, Claim claim)
    {
        claim.enterLeaveMessages = !claim.enterLeaveMessages;

        if (claim.enterLeaveMessages)
        {
            Message.FLAG_MESSAGES_ENABLED.send(executor);
        }
        else
        {
            Message.FLAG_MESSAGES_DISABLED.send(executor);
        }
    }

    public boolean messagesToggled(Claim claim)
    {
        return claim.enterLeaveMessages;
    }

    public void toggleContainers(Player executor, Claim claim)
    {
        if (worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.CHEST_ACCESS))
        {
            Message.FLAG_CONTAINERS_DISABLED.send(executor);

            claim.region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.DENY);
        }
        else
        {
            Message.FLAG_MESSAGES_ENABLED.send(executor);

            claim.region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW);
        }
    }

    public boolean containersToggled(Player executor, Claim claim)
    {
        return worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.CHEST_ACCESS);
    }

    public void toggleUse(Player executor, Claim claim)
    {
        if (worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.USE))
        {
            Message.FLAG_USE_DISABLED.send(executor);

            claim.region.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
        }
        else
        {
            Message.FLAG_MESSAGES_ENABLED.send(executor);

            claim.region.setFlag(DefaultFlag.USE, StateFlag.State.ALLOW);
        }
    }

    public boolean useToggled(Player executor, Claim claim)
    {
        return worldGuard.getRegionContainer().createQuery().testState(claim.centre, executor, DefaultFlag.USE);
    }

    private Set<Location> getOutlineLocations(Claim claim)
    {
        Location centre = claim.centre;
        Set<Location> locations = new HashSet<>();

        int radius = claim._tier.radius;

        for (int x = -radius; x <= radius; x ++)
        {
            for (int z = -radius; z <= radius; z ++)
            {
                if (Math.abs(x) != radius && Math.abs(z) != radius)
                {
                    continue;
                }

                // Only show at block level
                locations.add(new Location(centre.getWorld(), centre.getBlockX() + x, centre.getBlockY() + 0.5, centre.getBlockZ() + z));
            }
        }

        return locations;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        claims.forEach(claim -> claim.removeOutline(event.getPlayer()));
    }

    private BlockVector getMinimumPoint(Location centre, Tier tier)
    {
        Location minimum = centre.clone();

        minimum.setX(minimum.getBlockX() - tier.radius);
        minimum.setZ(minimum.getBlockZ() - tier.radius);

        minimum.setY(tier.skyToBedrock ? 1 : minimum.getBlockY() - tier.radius);

        return locationToVector(minimum);
    }

    private BlockVector getMaximumPoint(Location centre, Tier tier)
    {
        Location maximum = centre.clone();

        maximum.setX(maximum.getX() + tier.radius);
        maximum.setZ(maximum.getZ() + tier.radius);

        maximum.setY(tier.skyToBedrock ? 255 : maximum.getBlockY() + tier.radius);

        return locationToVector(maximum);
    }

    private BlockVector locationToVector(Location location)
    {
        return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private void setOwner(ProtectedRegion region, Player player)
    {
        DefaultDomain domain = new DefaultDomain();
        domain.addPlayer(local(player));

        region.setOwners(domain);
    }

    private void setDefaultFlags(ProtectedRegion region)
    {
        region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.DENY);
    }

    void enableGreetings(ProtectedRegion region, OfflinePlayer player, Tier tier)
    {
        region.setFlag(DefaultFlag.GREET_MESSAGE, MessageFormat.format(tier.greetingDefault, player.getName()));
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, MessageFormat.format(tier.farewellDefault, player.getName()));
    }

    public int getClaimCount(Player player)
    {
        return getPlayerRegions(player).size();
    }

    public int getPoints(Player player)
    {
        int points = getClaimCount(player);

        points += getPlayerRegions(player).stream()
            .flatMap(region -> region.getMembers().getUniqueIds().stream())
            .distinct()
            .count();

        return points;
    }

    private Set<ProtectedRegion> getPlayerRegions(Player player)
    {
        return getAllRegions().stream().filter(region -> region.isOwner(local(player)) && region.getId().startsWith("landclaims_")).collect(Collectors.toSet());
    }

    private Set<ProtectedRegion> getAllRegions()
    {
        Set<ProtectedRegion> regions = new HashSet<>();
        Bukkit.getWorlds().forEach(world -> regions.addAll(worldGuard.getRegionManager(world).getRegions().values()));

        return regions;
    }

    private LocalPlayer local(Player player)
    {
        return worldGuard.wrapPlayer(player);
    }
}
