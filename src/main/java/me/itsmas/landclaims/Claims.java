package me.itsmas.landclaims;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.itsmas.landclaims.claim.ClaimManager;
import me.itsmas.landclaims.command.CommandManager;
import me.itsmas.landclaims.hook.EconomyHook;
import me.itsmas.landclaims.hook.HolographHook;
import me.itsmas.landclaims.hook.PermissionsHook;
import me.itsmas.landclaims.hook.PlaceholderHook;
import me.itsmas.landclaims.menu.MenuData;
import me.itsmas.landclaims.menu.MenuManager;
import me.itsmas.landclaims.tier.TierManager;
import me.itsmas.landclaims.util.Items;
import me.itsmas.landclaims.util.Message;
import me.itsmas.landclaims.util.UtilServer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class Claims extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        preInit();
        createInjector();

        this.menuBackItem = Items.createStack(getConfig().getConfigurationSection("menu_back_button"));
    }

    @Override
    public void onDisable()
    {
        injector.getInstance(ClaimManager.class).save();
    }

    private void preInit()
    {
        saveDefaultConfig();
        Message.init(this);
    }

    private Injector injector;

    private ItemStack menuBackItem;

    public ItemStack getMenuBackItem()
    {
        return menuBackItem;
    }

    private void createInjector()
    {
        injector = Guice.createInjector(binder ->
        {
            binder.bind(Claims.class).toInstance(this);
            binder.bind(PaperPluginLogger.class).toInstance((PaperPluginLogger) getLogger());

            binder.bind(WorldGuardPlugin.class).toInstance(JavaPlugin.getPlugin(WorldGuardPlugin.class));

            binder.bind(ClaimsConfig.class).toInstance(new ClaimsConfig());

            binder.bind(TierManager.class).toInstance(new TierManager());
            binder.bind(ClaimManager.class).toInstance(new ClaimManager());
            binder.bind(MenuManager.class).toInstance(new MenuManager());

            binder.bind(EconomyHook.class).toInstance(new EconomyHook());
            binder.bind(PermissionsHook.class).toInstance(new PermissionsHook());
            binder.bind(HolographHook.class).toInstance(new HolographHook());

            binder.requestStaticInjection(UtilServer.class);
            binder.requestStaticInjection(MenuData.class);
        });

        PlaceholderHook placeholderHook = new PlaceholderHook(this);
        injector.injectMembers(placeholderHook);

        injector.getInstance(ClaimsConfig.class).init(this);

        injector.getInstance(TierManager.class).init();
        injector.getInstance(ClaimManager.class).init();
        injector.getInstance(MenuManager.class).init();

        injector.getInstance(EconomyHook.class).init();
        injector.getInstance(PermissionsHook.class).init();
        injector.getInstance(HolographHook.class).init();

        setupCommand();
    }

    private void setupCommand()
    {
        CommandManager command = new CommandManager();
        getCommand("landclaims").setExecutor(command);

        injector.injectMembers(command);

        command.init();
    }

    public <T> T getInstance(Class<T> clazz)
    {
        return injector.getInstance(clazz);
    }

    public void inject(Object object)
    {
        injector.injectMembers(object);
    }
}