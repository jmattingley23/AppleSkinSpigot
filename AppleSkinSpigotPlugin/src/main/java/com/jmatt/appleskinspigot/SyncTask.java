package com.jmatt.appleskinspigot;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.resources.MinecraftKey;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayer;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class SyncTask extends BukkitRunnable {

    private final MinecraftKey SATURATION_KEY = new MinecraftKey("appleskin", "saturation_sync");
    private final MinecraftKey EXHAUSTION_KEY = new MinecraftKey("appleskin", "exhaustion_sync");
    private final float MINIMUM_EXHAUSTION_CHANGE_THRESHOLD = 0.01F;

    private AppleSkinSpigotPlugin appleSkinSpigotPlugin;
    private BukkitCraftPlayer craftPlayer;
    private Map<UUID, Float> previousSaturationLevels;
    private Map<UUID, Float> previousExhaustionLevels;

    SyncTask(AppleSkinSpigotPlugin appleSkinSpigotPlugin, BukkitCraftPlayerFactory bukkitCraftPlayerFactory) throws UnsupportedMinecraftVersionException {
        this.appleSkinSpigotPlugin = appleSkinSpigotPlugin;
        previousSaturationLevels = new HashMap<>();
        previousExhaustionLevels = new HashMap<>();

        craftPlayer = bukkitCraftPlayerFactory.getBukkitCraftPlayer(appleSkinSpigotPlugin.getServer());
        if(craftPlayer == null) {
            throw new UnsupportedMinecraftVersionException();
        }
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
            craftPlayer.sendPacket(player, buildPacket(SATURATION_KEY, saturation));
            previousSaturationLevels.put(player.getUniqueId(), saturation);
        }

        float exhaustion = player.getExhaustion();
        Float previousExhaustion = previousExhaustionLevels.get(player.getUniqueId());
        if(previousExhaustion == null || Math.abs(exhaustion - previousExhaustion) >= MINIMUM_EXHAUSTION_CHANGE_THRESHOLD) {
            craftPlayer.sendPacket(player, buildPacket(EXHAUSTION_KEY, exhaustion));
            previousExhaustionLevels.put(player.getUniqueId(), exhaustion);
        }
    }

    private PacketPlayOutCustomPayload buildPacket(MinecraftKey minecraftKey, float value) {
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
        packetDataSerializer.writeFloat(value);
        return new PacketPlayOutCustomPayload(minecraftKey, packetDataSerializer);
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
