package com.jmatt.appleskinspigot;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener {

    private SyncTask syncTask;

    LoginListener(SyncTask syncTask) {
        this.syncTask = syncTask;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        syncTask.onPlayerLogIn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        syncTask.onPlayerLogOut(event.getPlayer());
    }

}
