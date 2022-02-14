package com.jmatt.appleskinspigot;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SyncTask extends BukkitRunnable {

    private final float MINIMUM_EXHAUSTION_CHANGE_THRESHOLD = 0.01F;

    private AppleSkinSpigotPlugin appleSkinSpigotPlugin;
    private Map<UUID, Float> previousSaturationLevels;
    private Map<UUID, Float> previousExhaustionLevels;

    SyncTask(AppleSkinSpigotPlugin appleSkinSpigotPlugin) {
        this.appleSkinSpigotPlugin = appleSkinSpigotPlugin;
        previousSaturationLevels = new HashMap<>();
        previousExhaustionLevels = new HashMap<>();
    }

    @Override
    public void run() {
        for(Player player : appleSkinSpigotPlugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        float saturation = player.getSaturation();
        Float previousSaturation = previousSaturationLevels.get(player.getUniqueId());
        if(previousSaturation == null || saturation != previousSaturation) {
            player.sendPluginMessage(appleSkinSpigotPlugin, AppleSkinSpigotPlugin.SATURATION_KEY, ByteBuffer.allocate(Float.BYTES).putFloat(saturation).array());
            previousSaturationLevels.put(player.getUniqueId(), saturation);
        }

        float exhaustion = player.getExhaustion();
        Float previousExhaustion = previousExhaustionLevels.get(player.getUniqueId());
        if(previousExhaustion == null || Math.abs(exhaustion - previousExhaustion) >= MINIMUM_EXHAUSTION_CHANGE_THRESHOLD) {
            player.sendPluginMessage(appleSkinSpigotPlugin, AppleSkinSpigotPlugin.EXHAUSTION_KEY, ByteBuffer.allocate(Float.BYTES).putFloat(exhaustion).array());
            previousExhaustionLevels.put(player.getUniqueId(), exhaustion);
        }
    }

    void onPlayerLogIn(Player player) {
        previousSaturationLevels.remove(player.getUniqueId());
        previousExhaustionLevels.remove(player.getUniqueId());
    }

    void onPlayerLogOut(Player player) {
        previousSaturationLevels.remove(player.getUniqueId());
        previousExhaustionLevels.remove(player.getUniqueId());
    }

}
