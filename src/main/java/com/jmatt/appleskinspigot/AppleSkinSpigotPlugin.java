package com.jmatt.appleskinspigot;

import org.bukkit.plugin.java.JavaPlugin;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayerFactory;

public class AppleSkinSpigotPlugin extends JavaPlugin {
    private SyncTask syncTask = null;

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            syncTask = createSyncTask();
        } catch (UnsupportedMinecraftVersionException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new LoginListener(syncTask), this);
        syncTask.runTaskTimer(this, 0L, 1L);
    }

    SyncTask createSyncTask() throws UnsupportedMinecraftVersionException {
        return new SyncTask(this, new BukkitCraftPlayerFactory());
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
