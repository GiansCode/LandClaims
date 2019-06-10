package me.itsmas.landclaims.command.commands;

import com.google.inject.Inject;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.ClaimsConfig;
import me.itsmas.landclaims.command.SubCommand;
import me.itsmas.landclaims.menu.MenuData;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Message;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand
{
    @Inject
    private Claims plugin;

    @Inject
    private TierManager tierManager;

    @Inject
    private ClaimsConfig config;

    public ReloadCommand()
    {
        super(Message.COMMAND_RELOAD_USAGE, "landclaims.command.reload", "reload");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        plugin.reloadConfig();

        Message.init(plugin);
        tierManager.loadTiers();
        config.init(plugin);
        MenuData.clearMenuCache();

        Message.COMMAND_RELOAD_SUCCESS.send(sender);
    }
}
