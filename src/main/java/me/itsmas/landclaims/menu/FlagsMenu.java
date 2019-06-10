package me.itsmas.landclaims.menu;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.Claim;
import me.itsmas.landclaims.claim.ClaimManager;
import me.itsmas.landclaims.claim.ClaimMenu;
import me.itsmas.landclaims.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class FlagsMenu extends Menu
{
    private final ClaimManager manager;

    public FlagsMenu(Claims plugin, Claim claim)
    {
        super(plugin, claim, MenuData.getData("flags"));

        this.manager = plugin.getInstance(ClaimManager.class);

        this.enabledStack = Items.createStack(plugin.getConfig().getConfigurationSection("misc.toggle_items.enabled"));
        this.disabledStack = Items.createStack(plugin.getConfig().getConfigurationSection("misc.toggle_items.disabled"));
    }

    private ItemStack enabledStack;
    private ItemStack disabledStack;

    @Override
    public boolean onClick(Player player, ItemStack stack, ClickType click, int slot)
    {
        if (stack.equals(enabledStack) || stack.equals(disabledStack))
        {
            stack = inventory.getItem(slot -= 9);
        }

        if (wasClicked(stack, "pvp"))
        {
            manager.togglePvp(player, claim);
            changeStack(slot);
        }
        else if (wasClicked(stack, "build"))
        {
            manager.toggleBuild(player, claim);
            changeStack(slot);
        }
        else if (wasClicked(stack, "messages"))
        {
            manager.toggleMessages(player, claim);
            changeStack(slot);
        }
        else if (wasClicked(stack, "containers"))
        {
            manager.toggleContainers(player, claim);
            changeStack(slot);
        }
        else if (wasClicked(stack, "use"))
        {
            manager.toggleUse(player, claim);
            changeStack(slot);
        }

        return false;
    }

    private void changeStack(int slot)
    {
        ItemStack stack = inventory.getItem(slot + 9);

        inventory.setItem(slot + 9, stack.equals(enabledStack) ? disabledStack : enabledStack);
    }

    @Override
    public void addButtons(Player player)
    {
        for (MenuButton button : menuData.getButtons())
        {
            addButton(button);
            ItemStack stack = button.getStack();

            boolean toggled =
                wasClicked(stack, "pvp") ? manager.pvpToggled(player, claim) :
                wasClicked(stack, "build") ? manager.buildToggled(player, claim) :
                wasClicked(stack, "messages") ? manager.messagesToggled(claim) :
                wasClicked(stack, "containers") ? manager.containersToggled(player, claim) :
                wasClicked(stack, "use") ? manager.useToggled(player, claim) : true;

            ItemStack toggleStack = toggled ? enabledStack : disabledStack;
            inventory.setItem(button.getSlot() + 9, toggleStack);
        }
    }

    @Override
    public Menu getBackMenu()
    {
        return ClaimMenu.getNewInstance(this);
    }
}
