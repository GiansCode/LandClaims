package me.itsmas.landclaims.menu;

import org.bukkit.inventory.ItemStack;

public class MenuButton
{
    private final String key;

    private final ItemStack stack;
    private final int slot;

    MenuButton(String key, ItemStack stack, int slot)
    {
        this.key = key;

        this.stack = stack;
        this.slot = slot;
    }

    String getKey()
    {
        return key;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    int getSlot()
    {
        return slot;
    }
}
