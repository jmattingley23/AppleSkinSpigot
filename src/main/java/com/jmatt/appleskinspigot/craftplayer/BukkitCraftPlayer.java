package com.jmatt.appleskinspigot.craftplayer;

import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import org.bukkit.entity.Player;

public interface BukkitCraftPlayer {

    void sendPacket(Player player, PacketPlayOutCustomPayload packet);

}
