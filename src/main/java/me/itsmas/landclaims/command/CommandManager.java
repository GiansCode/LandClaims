package me.itsmas.landclaims.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.command.commands.GiveAllCommand;
import me.itsmas.landclaims.command.commands.GiveCommand;
import me.itsmas.landclaims.command.commands.ReloadCommand;
import me.itsmas.landclaims.command.commands.TiersCommand;
import me.itsmas.landclaims.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class CommandManager implements CommandExecutor
{
    @Inject
    private Claims plugin;

    private final List<SubCommand> subCommands = new ArrayList<>();

    public void init()
    {
        registerSubCommand(new GiveCommand());
        registerSubCommand(new GiveAllCommand());

        registerSubCommand(new TiersCommand());

        registerSubCommand(new ReloadCommand());
    }

    private void registerSubCommand(SubCommand command)
    {
        subCommands.add(command);
        plugin.inject(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("landclaims.command"))
        {
            Message.NO_PERMISSION.send(sender);
            return true;
        }

        if (args.length == 0)
        {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        for (SubCommand subCommand : subCommands)
        {
            if (subCommand.isAlias(sub))
            {
                if (subCommand.hasPermission(sender, true))
                {
                    subCommand.execute(sender, args);
                }

                return true;
            }
        }

        sendUsage(sender);
        return true;
    }

    private void sendUsage(CommandSender sender)
    {
        Message.COMMAND_USAGE_HEADER.send(sender);

        for (SubCommand subCommand : subCommands)
        {
            if (subCommand.hasPermission(sender, false))
            {
                subCommand.sendUsage(sender);
            }
        }
    }
}
