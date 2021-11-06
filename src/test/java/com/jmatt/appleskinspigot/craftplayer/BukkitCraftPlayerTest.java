package com.jmatt.appleskinspigot.craftplayer;

import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BukkitCraftPlayerTest {

    @Mock
    EntityPlayer entityPlayerMock;
    @Mock
    PlayerConnection playerConnectionMock;
    @Mock
    PacketPlayOutCustomPayload packetMock;

    Player playerMock;

    @Test
    public void sendPacket1_17_1SendsPacket() {
        playerMock = Mockito.mock(CraftPlayer.class);
        when(((CraftPlayer)playerMock).getHandle()).thenReturn(entityPlayerMock);
        entityPlayerMock.b = playerConnectionMock;
        new BukkitCraftPlayer_1_17_1().sendPacket(playerMock, packetMock);
        verify(playerConnectionMock, times(1)).sendPacket(packetMock);
    }

}
