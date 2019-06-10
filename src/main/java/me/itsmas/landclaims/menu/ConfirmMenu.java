package me.itsmas.landclaims.menu;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.ClaimMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmMenu extends Menu
{
    private final Runnable listener;

    public ConfirmMenu(Claims plugin, Runnable listener, String action)
    {
        super(plugin, null, MenuData.getData("confirm").formatName(action));

        this.listener = listener;
    }

    @Override
    public boolean onClick(Player player, ItemStack stack, ClickType click, int slot)
    {
        if (wasClicked(stack, "confirm"))
        {
            listener.run();
            player.closeInventory();
        }
        else if (wasClicked(stack, "cancel"))
        {
            player.closeInventory();
        }

        return false;
    }

    @Override
    public Menu getBackMenu()
    {
        return ClaimMenu.getNewInstance(this);
    }
}
