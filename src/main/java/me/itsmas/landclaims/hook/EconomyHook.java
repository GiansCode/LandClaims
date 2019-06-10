package me.itsmas.landclaims.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.itsmas.landclaims.ClaimsConfig;
import me.itsmas.landclaims.util.Message;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

@Singleton
public class EconomyHook
{
    @Inject
    private ClaimsConfig config;

    private Economy economy;

    public void init()
    {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (provider != null)
        {
            economy = provider.getProvider();
        }
    }

    public boolean takeMoney(Player player)
    {
        if (!config.economyEnabled)
        {
            return true;
        }

        double balance = economy.getBalance(player);
        double required = config.economyPrice;

        if (balance >= required)
        {
            economy.withdrawPlayer(player, required);
            return true;
        }

        Message.INSUFFICIENT_FUNDS.send(player, required, balance);
        return false;
    }
}
