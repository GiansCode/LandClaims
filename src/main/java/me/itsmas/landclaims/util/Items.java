package me.itsmas.landclaims.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.itsmas.landclaims.util.Colour.translate;

public final class Items
{
    private Items() {}

    public static void giveItem(Player player, ItemStack stack)
    {
        if (player.getInventory().firstEmpty() == -1)
        {
            player.getWorld().dropItem(player.getLocation(), stack);
            Message.ITEM_DROPPED.send(player);

            return;
        }

        player.getInventory().addItem(stack);
    }

    public static ItemStack createStack(FileConfiguration config)
    {
        return createStack(config.getConfigurationSection("stack"));
    }

    public static String getItemName(ItemStack stack)
    {
        if (stack == null)
        {
            return "";
        }

        if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
        {
            return stack.getItemMeta().getDisplayName();
        }

        return stack.getType().name();
    }

    public static ItemStack createStack(ConfigurationSection config)
    {
        try
        {
            ItemStack stack = new ItemStack(getMaterial(config), 1, (short) getData(config));
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(getName(config));
            meta.setLore(getLore(config));
            meta.addItemFlags(getFlags(config));

            stack.addUnsafeEnchantments(getEnchantments(config));

            stack.setItemMeta(meta);

            return stack;
        }
        catch (Exception ex)
        {
            UtilServer.getPlugin().getLogger().severe("Could not load ItemStack from file");
            ex.printStackTrace();

            return new ItemStack(Material.STONE);
        }
    }

    private static Material getMaterial(ConfigurationSection config) throws IllegalArgumentException
    {
        return Material.valueOf(config.getString("material", "STONE").toUpperCase());
    }

    private static int getData(ConfigurationSection config)
    {
        return config.getInt("data", 0);
    }

    private static String getName(ConfigurationSection config)
    {
        return translate(config.getString("name", "Undefined Name :O"));
    }

    private static List<String> getLore(ConfigurationSection config)
    {
        List<String> lore = config.getStringList("lore");
        lore.replaceAll(Colour::translate);

        return lore;
    }

    private static Map<Enchantment, Integer> getEnchantments(ConfigurationSection config) throws IllegalArgumentException
    {
        List<String> list = config.getStringList("enchantments");
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        for (String item : list)
        {
            String[] split = item.split(";");

            Enchantment enchantment = Enchantment.getByName(split[0].toUpperCase());
            int level = Integer.parseInt(split[1]);

            if (enchantment != null)
            {
                enchantments.put(enchantment, level);
            }
        }

        return enchantments;
    }

    private static ItemFlag[] getFlags(ConfigurationSection config) throws IllegalArgumentException
    {
        List<String> list = config.getStringList("flags");
        Set<ItemFlag> flags = new HashSet<>();

        for (String item : list)
        {
            ItemFlag flag = ItemFlag.valueOf(item.toUpperCase());

            flags.add(flag);
        }

        return flags.toArray(new ItemFlag[0]);
    }
}
