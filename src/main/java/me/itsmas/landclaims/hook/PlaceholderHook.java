package me.itsmas.landclaims.hook;

import com.google.inject.Inject;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.itsmas.landclaims.Claims;
import me.itsmas.landclaims.claim.ClaimManager;
import org.bukkit.entity.Player;

public class PlaceholderHook extends EZPlaceholderHook
{
    @Inject
    private ClaimManager claimManager;

    @Inject
    private PermissionsHook permissionsHook;

    public PlaceholderHook(Claims plugin)
    {
        super(plugin, "landclaims");
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier)
    {
        if (player == null)
        {
            return null;
        }

        if (identifier.equals("claims"))
        {
            return String.valueOf(claimManager.getClaimCount(player));
        }

        if (identifier.equals("max_claims"))
        {
            return String.valueOf(permissionsHook.getMaxClaims(player));
        }

        if (identifier.equals("points"))
        {
            return String.valueOf(claimManager.getPoints(player));
        }

        return null;
    }
}
