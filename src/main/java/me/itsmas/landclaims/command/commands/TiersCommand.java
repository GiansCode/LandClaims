package me.itsmas.landclaims.command.commands;

import com.google.inject.Inject;
import me.itsmas.landclaims.command.SubCommand;
import me.itsmas.landclaims.tier.Tier;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class TiersCommand extends SubCommand
{
    @Inject
    private TierManager tierManager;

    public TiersCommand()
    {
        super(Message.COMMAND_TIERS_USAGE, "landclaims.command.tiers", "tiers");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Message.COMMAND_TIERS_HEADER.send(sender);

        for (Tier tier : tierManager.getTiers())
        {
            TextComponent component = new TextComponent(Message.COMMAND_TIERS_SUCCESS.format(tier.tier));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/landclaims give " + sender.getName() + " " + tier.tier));

            sender.spigot().sendMessage(component);
        }
    }
}
