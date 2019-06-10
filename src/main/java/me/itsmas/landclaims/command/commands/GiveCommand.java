package me.itsmas.landclaims.command.commands;

import com.google.inject.Inject;
import me.itsmas.landclaims.command.SubCommand;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends SubCommand
{
    @Inject
    private TierManager tierManager;

    public GiveCommand()
    {
        super(Message.COMMAND_GIVE_USAGE, "landclaims.command.give", "give");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (args.length < 3 || args.length > 4)
        {
            sendUsage(sender);
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);

        if (player == null)
        {
            Message.PLAYER_OFFLINE.send(sender);
            return;
        }

        Tier tier = parseTier(tierManager, sender, args[2]);

        if (tier == null)
        {
            return;
        }

        int amount = parseInt(sender, args, 3);

        if (amount == -1)
        {
            return;
        }

        Message.COMMAND_GIVE_SUCCESS.send(sender, amount, tier, player.getName());
        giveTier(sender, player, tier, amount);
    }
}
