package me.itsmas.landclaims.menu;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.Claim;
import me.itsmas.landclaims.claim.ClaimMenu;
import me.itsmas.landclaims.util.Message;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class MemberMenu extends Menu
{
    public MemberMenu(Claims plugin, Claim claim)
    {
        super(plugin, claim, MenuData.getData("members"));
    }

    @Override
    public boolean onClick(Player player, ItemStack stack, ClickType click, int slot)
    {
        if (wasClicked(stack, "add"))
        {
            new AnvilGUI(plugin, player, "Player to add", (p, input) ->
            {
                claim.tryAddMember(player, Bukkit.getOfflinePlayer(input));

                return null;
            });
        }
        else if (wasClicked(stack, "list"))
        {
            if (claim.region.getMembers().size() == 0)
            {
                Message.NO_ADDED_MEMBERS.send(player);
                return false;
            }

            new MemberListMenu(plugin, claim).open(player);
        }
        else if (wasClicked(stack, "remove"))
        {
            new AnvilGUI(plugin, player, "Player to remove", (p, input) ->
            {
                claim.tryRemoveMember(player, Bukkit.getOfflinePlayer(input));

                return null;
            });
        }

        return false;
    }

    @Override
    public Menu getBackMenu()
    {
        return ClaimMenu.getNewInstance(this);
    }
}
