package me.itsmas.landclaims.menu;

import com.google.inject.Inject;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.Claim;
import me.itsmas.landclaims.util.Items;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.itsmas.landclaims.util.Colour.translate;

public class MenuData
{
    @Inject
    private static Claims PLUGIN;

    private static Map<String, MenuData> MENU_CACHE = new HashMap<>();

    public static void clearMenuCache()
    {
        MENU_CACHE.clear();
    }

    public static MenuData getData(String name)
    {
        MenuData data = MENU_CACHE.get(name);

        if (data == null)
        {
            data = new MenuData(PLUGIN, name);
            MENU_CACHE.put(name, data);
        }

        return data;
    }

    private final Claims plugin;

    private String title;
    private final int size;

    private final List<MenuButton> buttons = new ArrayList<>();

    String getTitle()
    {
        return title;
    }

    int getSize()
    {
        return size;
    }

    List<MenuButton> getButtons()
    {
        return Collections.unmodifiableList(buttons);
    }

    private MenuData(Claims plugin, String key)
    {
        this(plugin, key,
            translate(plugin.getConfig().getString("menus." + key + ".title")),
            plugin.getConfig().getInt("menus." + key + ".size")
        );
    }

    public static MenuData getMemberListData(Claims plugin, Claim claim)
    {
        String key = "member_list";

        int size = claim.region.getMembers().size();
        size = (int) Math.ceil((double) size / 9);

        String title = MenuData.getData(key).getTitle();

        return new MenuData(plugin, key, title, size);
    }


    private MenuData(Claims plugin, String key, String title, int size)
    {
        this.plugin = plugin;

        this.title = title;
        this.size = size;

        if (!key.equals("member_list"))
        {
            parseButtons(key);
        }
    }

    private void parseButtons(String key)
    {
        ConfigurationSection buttons = plugin.getConfig().getConfigurationSection("menus." + key + ".buttons");

        for (String button : buttons.getKeys(false))
        {
            ConfigurationSection buttonSect = buttons.getConfigurationSection(button);

            ItemStack stack = Items.createStack(buttonSect.getConfigurationSection("stack"));
            int slot = buttonSect.getInt("slot");

            this.buttons.add(new MenuButton(button, stack, slot));
        }
    }

    public MenuData formatName(Object... params)
    {
        title = MessageFormat.format(title, params);

        return this;
    }
}
