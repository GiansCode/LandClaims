package me.itsmas.landclaims.command.commands;

import com.google.inject.Inject;
import me.itsmas.landclaims.command.SubCommand;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class GiveAllCommand extends SubCommand
{
    @Inject
    private TierManager tierManager;

    public GiveAllCommand()
    {
        super(Message.COMMAND_GIVEALL_USAGE, "landclaims.command.giveall", "giveall");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (args.length < 2 || args.length > 3)
        {
            sendUsage(sender);
            return;
        }

        Tier tier = parseTier(tierManager, sender, args[1]);

        if (tier == null)
        {
            return;
        }

        int amount = parseInt(sender, args, 2);

        if (amount == -1)
        {
            return;
        }

        Message.COMMAND_GIVEALL_SUCCESS.send(sender, amount, tier);
        Bukkit.getOnlinePlayers().forEach(player -> giveTier(sender, player, tier, amount));
    }
}
