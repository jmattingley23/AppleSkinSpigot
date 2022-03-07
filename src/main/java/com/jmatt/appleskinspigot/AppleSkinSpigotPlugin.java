package com.jmatt.appleskinspigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public class AppleSkinSpigotPlugin extends JavaPlugin {

    public static final String SATURATION_KEY = "appleskin:saturation_sync";
    public static final String EXHAUSTION_KEY = "appleskin:exhaustion_sync";

    private SyncTask syncTask = null;

    @Override
    public void onEnable() {
        syncTask = createSyncTask();

        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        syncTask.runTaskTimer(this, 0L, 1L);

        Messenger messenger = getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, SATURATION_KEY);
        messenger.registerOutgoingPluginChannel(this, EXHAUSTION_KEY);
    }

    SyncTask createSyncTask() {
        return new SyncTask(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if(syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

}
