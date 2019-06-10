package me.itsmas.landclaims.menu;

import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.Claim;
import me.itsmas.landclaims.claim.ClaimMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MemberListMenu extends Menu
{
    MemberListMenu(Claims plugin, Claim claim)
    {
        super(plugin, claim, MenuData.getMemberListData(plugin, claim));

        claim.region.getMembers().getUniqueIds().forEach(uuid ->
        {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            meta.setDisplayName(ChatColor.RESET + name);
            meta.setOwningPlayer(offlinePlayer);

            skull.setItemMeta(meta);

            inventory.addItem(skull);
        });
    }

    @Override
    public boolean onClick(Player player, ItemStack stack, ClickType click, int slot)
    {
        return false;
    }

    @Override
    public Menu getBackMenu()
    {
        return ClaimMenu.getNewInstance(this);
    }
}
