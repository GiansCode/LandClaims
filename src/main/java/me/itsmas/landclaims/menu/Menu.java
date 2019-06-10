package me.itsmas.landclaims.menu;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.Claim;
import me.itsmas.landclaims.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class Menu
{
    public final Claims plugin;

    public final Claim claim;
    final MenuData menuData;

    protected final Inventory inventory;

    private final boolean backButton;

    Menu(Claims plugin, Claim claim, MenuData menuData)
    {
        this(plugin, claim, menuData, true);
    }

    public Menu(Claims plugin, Claim claim, MenuData menuData, boolean backButton)
    {
        this.plugin = plugin;

        this.claim = claim;
        this.menuData = menuData;

        this.inventory = Bukkit.createInventory(null, menuData.getSize() * 9, menuData.getTitle());

        this.backButton = backButton;
    }

    private void addBackButton()
    {
        inventory.setItem(inventory.getSize() - 5, plugin.getMenuBackItem());
    }

    private List<MenuButton> buttons = new ArrayList<>();

    public void open(Player player)
    {
        if (backButton)
        {
            addBackButton();
        }

        addButtons(player);
        player.openInventory(inventory);

        plugin.getInstance(MenuManager.class).updateMenu(player, this);
    }

    void addButtons(Player player)
    {
        menuData.getButtons().forEach(this::addButton);
    }

    void addButton(MenuButton button)
    {
        buttons.add(button);
        inventory.setItem(button.getSlot(), button.getStack());
    }

    /**
     * @return True if the player can take the item
     */
    public abstract boolean onClick(Player player, ItemStack stack, ClickType click, int slot);

    public abstract Menu getBackMenu();

    protected boolean wasClicked(ItemStack stack, String buttonKey)
    {
        MenuButton button = getButton(buttonKey);

        assert button != null : "Invalid button key";

        return
                stack.getType() == button.getStack().getType() &&
                        Items.getItemName(stack).equals(Items.getItemName(button.getStack()));
    }

    private MenuButton getButton(String name)
    {
        for (MenuButton button : buttons)
        {
            if (button.getKey().equals(name))
            {
                return button;
            }
        }

        // Shouldn't happen
        return null;
    }
}
