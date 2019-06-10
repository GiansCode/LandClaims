package me.itsmas.landclaims.claim;

import com.google.inject.Inject;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.menu.MenuData;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import me.itsmas.landclaims.util.UtilServer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

class ClaimListener implements Listener
{
    @Inject
    private ClaimManager manager;

    @Inject
    private Claims plugin;

    @Inject
    private TierManager tierManager;

    ClaimListener()
    {
        UtilServer.registerListener(this);

        mainMenuData = MenuData.getData("main");
    }

    private MenuData mainMenuData;

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Tier tier = tierManager.getByStack(event.getItemInHand());

        if (tier != null)
        {
            if (manager.canCreateClaim(player, location))
            {
                ProtectedRegion region = manager.createRegion(player, location, tier);

                if (region == null)
                {
                    cancelEvent(event);
                }
            }
            else
            {
                cancelEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCheckPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
        {
            Claim claim = manager.getClaimFromCentre(event.getBlock());

            if (claim == null)
            {
                return;
            }

            ProtectedRegion region = claim.region;
            Player player = event.getPlayer();

            if (canPlaceOrBreak(player, region))
            {
                event.setBuild(true);
                event.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCheckBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
        {
            Claim claim = manager.getClaimFromCentre(event.getBlock());

            if (claim == null)
            {
                return;
            }

            ProtectedRegion region = claim.region;
            Player player = event.getPlayer();

            if (canPlaceOrBreak(player, region))
            {
                event.setCancelled(false);
            }
        }
    }

    private boolean canPlaceOrBreak(Player player, ProtectedRegion region)
    {
        return region.getOwners().contains(player.getUniqueId()) || region.getMembers().contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND)
        {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        Claim claim = manager.getClaimFromCentre(block);

        if (claim != null && claim.canUseMenu(player))
        {
            new ClaimMenu(plugin, manager, claim, mainMenuData).open(player);
        }
    }

    private void cancelEvent(BlockPlaceEvent event)
    {
        event.setBuild(false);
        event.setCancelled(true);
    }

    /* Stop region centre destroy */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();

        if (manager.isRegionCentre(event.getBlock()))
        {
            Message.CANNOT_BREAK_CENTRE.send(player);

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFace(BlockFadeEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        if (manager.isRegionCentre(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    // Region enter/leave listening
    private final Map<Player, Claim> claimMap = new HashMap<>();

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event)
    {
        if (event.isCancelled() || !isValidMove(event))
        {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getTo();

        // Claim entering/leaving logic
        Claim claim = manager.getClaimFromLocation(location);
        Claim previousClaim = claimMap.get(player);

        if (previousClaim == null && claim != null)
        {
            manager.sendClaimEnterMessage(player, claim);
            claimMap.put(player, claim);

            return;
        }

        if (claim == null && previousClaim != null)
        {
            manager.sendClaimLeaveMessage(player, previousClaim);
            claimMap.put(player, null);

            return;
        }

        if (claim != null)
        {
            boolean sameOwner = previousClaim.getOwner().getUniqueId().equals(claim.getOwner().getUniqueId());

            if (!sameOwner)
            {
                manager.sendClaimLeaveMessage(player, previousClaim);
                manager.sendClaimEnterMessage(player, claim);

                claimMap.put(player, claim);
            }
        }
    }

    private boolean isValidMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();

        return
            from.getX() != to.getX() ||
            from.getY() != to.getY() ||
            from.getZ() != to.getZ();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        claimMap.remove(event.getPlayer());
    }
}
