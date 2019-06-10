package me.itsmas.landclaims.util;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.text.MessageFormat;
import java.util.List;

import static me.itsmas.landclaims.util.Colour.translate;

public enum Message
{
    COMMAND_USAGE_HEADER,

    COMMAND_GIVE_USAGE,
    COMMAND_GIVE_SUCCESS,

    COMMAND_GIVEALL_USAGE,
    COMMAND_GIVEALL_SUCCESS,

    COMMAND_GIVE_GENERIC_RECEIVED,

    COMMAND_TIERS_USAGE,
    COMMAND_TIERS_HEADER,
    COMMAND_TIERS_SUCCESS,

    COMMAND_RELOAD_USAGE,
    COMMAND_RELOAD_SUCCESS,

    NO_DROP_TIER,

    MAX_CLAIMS_EXCEEDED,
    INSIDE_EXISTING,
    CLAIM_OVERLAP,
    INSUFFICIENT_FUNDS,

    CLAIM_CREATED,

    NO_MENU_PERMISSION,

    CANNOT_BREAK_CENTRE,

    CLAIM_DELETED,

    OUTLINE_ENABLED,
    OUTLINE_DISABLED,

    MEMBER_ADDED,
    MEMBER_ALREADY_ADDED,
    ADD_NO_SELF,

    NO_ADDED_MEMBERS,

    MEMBER_REMOVED,
    MEMBER_NOT_A_MEMBER,
    REMOVE_NO_SELF,

    OWNERSHIP_CHANGED,
    OWNER_NOT_JOINED,
    ALREADY_OWNS,

    FLAG_PVP_ENABLED,
    FLAG_PVP_DISABLED,

    FLAG_BUILD_ENABLED,
    FLAG_BUILD_DISABLED,

    FLAG_MESSAGES_ENABLED,
    FLAG_MESSAGES_DISABLED,

    FLAG_CONTAINERS_ENABLED,
    FLAG_CONTAINERS_DISABLED,

    FLAG_USE_ENABLED,
    FLAG_USE_DISABLED,

    ITEM_DROPPED,
    INVALID_TIER,
    NO_PERMISSION,
    PLAYER_OFFLINE,
    INVALID_INTEGER,
    ;

    private String value = name();

    public void send(CommandSender sender, Object... args)
    {
        if (value != null)
        {
            sender.sendMessage(format(args));
        }
    }

    public String format(Object... args)
    {
        return MessageFormat.format(value, args);
    }

    @SuppressWarnings("unchecked")
    public static void init(Plugin plugin)
    {
        for (Message message : values())
        {
            Object object = plugin.getConfig().get("messages." + message.name().toLowerCase());

            if (object == null)
            {
                plugin.getLogger().severe("Value missing for message " + message.name());
                continue;
            }

            String value =
                    object instanceof String ? (String) object :
                            object instanceof List ? String.join("\n", (List<String>) object) :
                                null;

            if (value == null)
            {
                plugin.getLogger().severe("Invalid data type for message " + message.name());
                continue;
            }

            message.value = translate(value);
        }
    }
}
