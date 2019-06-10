package me.itsmas.landclaims.claim;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.menu.ConfirmMenu;
import me.itsmas.landclaims.menu.FlagsMenu;
import me.itsmas.landclaims.menu.MemberMenu;
import me.itsmas.landclaims.menu.Menu;
import me.itsmas.landclaims.menu.MenuData;
import me.itsmas.landclaims.util.Message;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ClaimMenu extends Menu
{
    private final ClaimManager manager;

    ClaimMenu(Claims plugin, ClaimManager manager, Claim claim, MenuData menuData)
    {
        super(plugin, claim, menuData, false);

        this.manager = manager;
    }

    public static ClaimMenu getNewInstance(Menu menu)
    {
        return new ClaimMenu(
            menu.plugin, menu.plugin.getInstance(ClaimManager.class), menu.claim, MenuData.getData("main")
        );
    }

    @Override
    public boolean onClick(Player player, ItemStack stack, ClickType click, int slot)
    {
        if (wasClicked(stack, "outline"))
        {
            player.closeInventory();
            plugin.getInstance(ClaimManager.class).toggleOutline(player, claim);
        }
        else if (wasClicked(stack, "members"))
        {
            new MemberMenu(plugin, claim).open(player);
        }
        else if (wasClicked(stack, "flags"))
        {
            new FlagsMenu(plugin, claim).open(player);
        }
        else if (wasClicked(stack, "delete"))
        {
            new ConfirmMenu(plugin, () ->
            {
                manager.deleteClaim(player, claim);

                player.closeInventory();
            }, "Delete Claim").open(player);
        }
        else if (wasClicked(stack, "transfer_ownership"))
        {
            new AnvilGUI(plugin, player, "New Owner", (p, input) ->
            {
                OfflinePlayer target = Bukkit.getOfflinePlayer(input);

                if (!target.isOnline() && !target.hasPlayedBefore())
                {
                    Message.OWNER_NOT_JOINED.send(player);
                    return null;
                }

                if (target.getUniqueId().equals(player.getUniqueId()))
                {
                    Message.ALREADY_OWNS.send(player);
                    return null;
                }

                new ConfirmMenu(plugin, () -> claim.transferOwnership(player, target), "Transfer Owner").open(player);
                return null;
            });
        }

        return false;
    }

    @Override
    public Menu getBackMenu()
    {
        return null;
    }
}
