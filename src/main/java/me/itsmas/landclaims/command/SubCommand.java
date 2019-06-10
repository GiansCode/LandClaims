package me.itsmas.landclaims.command;

import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Items;
import me.itsmas.landclaims.util.Message;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SubCommand
{
    private final Message usageMessage;
    private final String permission;
    private final String[] aliases;

    public SubCommand(Message usageMessage, String permission, String... aliases)
    {
        this.usageMessage = usageMessage;
        this.permission = permission;
        this.aliases = aliases;
    }

    boolean isAlias(String alias)
    {
        return ArrayUtils.contains(aliases, alias);
    }

    boolean hasPermission(CommandSender sender, boolean inform)
    {
        if (!sender.hasPermission(permission))
        {
            if (inform)
            {
                Message.NO_PERMISSION.send(sender);
            }
            return false;
        }

        return true;
    }

    protected void sendUsage(CommandSender sender)
    {
        usageMessage.send(sender);
    }

    public abstract void execute(CommandSender sender, String[] args);

    /* Helpful Methods */
    protected int parseInt(CommandSender sender, String[] args, int index)
    {
        if (args.length <= index)
        {
            return 1;
        }

        try
        {
            return Integer.parseInt(args[index]);
        }
        catch (NumberFormatException ex)
        {
            Message.INVALID_INTEGER.send(sender);
            return -1;
        }
    }

    protected void giveTier(CommandSender sender, Player receiver, Tier tier, int amount)
    {
        ItemStack stack = tier.stack.clone();
        stack.setAmount(amount);

        Items.giveItem(receiver, stack);
        Message.COMMAND_GIVE_GENERIC_RECEIVED.send(receiver, amount, tier, sender.getName());
    }

    protected Tier parseTier(TierManager tierManager, CommandSender sender, String input)
    {
        Tier tier = tierManager.getTierByName(input);

        if (tier == null)
        {
            Message.INVALID_TIER.send(sender);
            return null;
        }

        return tier;
    }
}
