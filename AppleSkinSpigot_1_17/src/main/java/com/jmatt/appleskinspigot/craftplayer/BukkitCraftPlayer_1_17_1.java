package com.jmatt.appleskinspigot.craftplayer;

import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BukkitCraftPlayer_1_17_1 implements BukkitCraftPlayer {

    @Override
    public void sendPacket(Player player, PacketPlayOutCustomPayload packet) {
        ((CraftPlayer)player).getHandle().b.sendPacket(packet);
    }

}
