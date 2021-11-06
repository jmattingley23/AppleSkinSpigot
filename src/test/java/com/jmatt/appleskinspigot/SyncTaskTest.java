package com.jmatt.appleskinspigot;

import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.resources.MinecraftKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayer;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyFloat;
import static org.mockito.Mockito.doReturn;

@PrepareForTest(SyncTask.class)
@RunWith(PowerMockRunner.class)
public class SyncTaskTest {

    @Mock
    AppleSkinSpigotPlugin pluginMock;
    @Mock
    Server serverMock;
    @Mock
    BukkitCraftPlayerFactory craftPlayerFactoryMock;
    @Mock
    BukkitCraftPlayer craftPlayerMock;

    float saturation = 5F;
    float exhaustion = 1.8F;
    Player playerMock;
    PacketPlayOutCustomPayload saturationPacketMock;
    PacketPlayOutCustomPayload exhaustionPacketMock;
    UUID playerId;

    @Before
    public void setUp() {
        when(pluginMock.getServer()).thenReturn(serverMock);
        when(craftPlayerFactoryMock.getBukkitCraftPlayer(serverMock)).thenReturn(craftPlayerMock);

        playerMock = mock(Player.class);
        when(playerMock.getSaturation()).thenReturn(saturation);
        when(playerMock.getExhaustion()).thenReturn(exhaustion);
        saturationPacketMock = mock(PacketPlayOutCustomPayload.class);
        exhaustionPacketMock = mock(PacketPlayOutCustomPayload.class);

        playerId = UUID.randomUUID();
        when(playerMock.getUniqueId()).thenReturn(playerId);
    }

    @Test
    public void syncTaskCreatesBukkitPlayer() throws UnsupportedMinecraftVersionException {
        new SyncTask(pluginMock, craftPlayerFactoryMock);
        Mockito.verify(craftPlayerFactoryMock, times(1)).getBukkitCraftPlayer(serverMock);
    }

    @Test(expected = UnsupportedMinecraftVersionException.class)
    public void nullBukkitPlayerThrowsException() throws UnsupportedMinecraftVersionException {
        when(craftPlayerFactoryMock.getBukkitCraftPlayer(serverMock)).thenReturn(null);
        new SyncTask(pluginMock, craftPlayerFactoryMock);
        Mockito.verify(craftPlayerFactoryMock, times(1)).getBukkitCraftPlayer(serverMock);
    }

    @Test
    public void updatePlayerSendsPacket() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        PowerMockito.doReturn(saturationPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(saturation));
        PowerMockito.doReturn(exhaustionPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(exhaustion));

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(craftPlayerMock, atLeastOnce()).sendPacket(playerMock, saturationPacketMock);
        verify(craftPlayerMock, atLeastOnce()).sendPacket(playerMock, exhaustionPacketMock);
        PowerMockito.verifyPrivate(syncTask, times(2)).invoke("buildPacket", any(MinecraftKey.class), anyFloat());
    }

    @Test
    public void updatePlayerSkipsSaturationIfUnchanged() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        Map<UUID, Float> previousSaturationLevels = Whitebox.getInternalState(syncTask, "previousSaturationLevels");
        previousSaturationLevels.put(playerId, saturation);
        PowerMockito.doReturn(saturationPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(saturation));
        PowerMockito.doReturn(exhaustionPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(exhaustion));

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(craftPlayerMock, never()).sendPacket(playerMock, saturationPacketMock);
        verify(craftPlayerMock, times(1)).sendPacket(playerMock, exhaustionPacketMock);
        PowerMockito.verifyPrivate(syncTask, times(1)).invoke("buildPacket", any(MinecraftKey.class), anyFloat());
    }

    @Test
    public void updatePlayerSkipsExhaustionIfBelowThreshold() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        Map<UUID, Float> previousExhaustionLevels = Whitebox.getInternalState(syncTask, "previousExhaustionLevels");
        previousExhaustionLevels.put(playerId, exhaustion-.001F);
        PowerMockito.doReturn(saturationPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(saturation));
        PowerMockito.doReturn(exhaustionPacketMock).when(syncTask, "buildPacket", any(MinecraftKey.class), eq(exhaustion));

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(craftPlayerMock, times(1)).sendPacket(playerMock, saturationPacketMock);
        verify(craftPlayerMock, never()).sendPacket(playerMock, exhaustionPacketMock);
        PowerMockito.verifyPrivate(syncTask, times(1)).invoke("buildPacket", any(MinecraftKey.class), anyFloat());
    }

    @Test
    public void buildPacketBuildsPacket() throws Exception {
        SyncTask syncTask = new SyncTask(pluginMock, craftPlayerFactoryMock);
        MinecraftKey minecraftKey = mock(MinecraftKey.class);
        float packetValue = 2F;
        PacketPlayOutCustomPayload result = Whitebox.invokeMethod(syncTask, "buildPacket", minecraftKey, packetValue);
        assertEquals(minecraftKey, result.b());
        assertEquals(packetValue, result.c().getFloat(0), 0.001);
    }

    @Test
    public void runUpdatesAllOnlinePlayers() throws Exception {
        Player firstPlayer = Whitebox.newInstance(Player.class);
        Player secondPlayer = Whitebox.newInstance(Player.class);
        List<Player> players = List.of(firstPlayer, secondPlayer);
        doReturn(players).when(serverMock).getOnlinePlayers();

        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        PowerMockito.doNothing().when(syncTask, "updatePlayer", any(Player.class));
        syncTask.run();
        PowerMockito.verifyPrivate(syncTask, times(players.size())).invoke("updatePlayer", any(Player.class));
    }

    @Test
    public void playerLogInRemovesPreviousLevels() throws UnsupportedMinecraftVersionException {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        syncTask.onPlayerLogIn(playerMock);

        Map<UUID, Float> previousSaturationLevels =  Whitebox.getInternalState(syncTask, "previousSaturationLevels");
        previousSaturationLevels.put(playerId, 5F);
        Map<UUID, Float> previousExhaustionLevels = Whitebox.getInternalState(syncTask, "previousExhaustionLevels");
        previousExhaustionLevels.put(playerId, 2.1F);
        Whitebox.setInternalState(syncTask, "previousSaturationLevels", previousSaturationLevels);
        Whitebox.setInternalState(syncTask, "previousExhaustionLevels", previousExhaustionLevels);

        assertEquals(1, previousSaturationLevels.size());
        assertEquals(1, previousExhaustionLevels.size());
        syncTask.onPlayerLogOut(playerMock);
        assertEquals(0, previousSaturationLevels.size());
        assertEquals(0, previousExhaustionLevels.size());
    }

    @Test
    public void playerLogOutRemovesPreviousLevels() throws UnsupportedMinecraftVersionException {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock, craftPlayerFactoryMock));
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        syncTask.onPlayerLogOut(player);

        Map<UUID, Float> previousSaturationLevels =  Whitebox.getInternalState(syncTask, "previousSaturationLevels");
        previousSaturationLevels.put(playerId, 5F);
        Map<UUID, Float> previousExhaustionLevels = Whitebox.getInternalState(syncTask, "previousExhaustionLevels");
        previousExhaustionLevels.put(playerId, 2.1F);
        Whitebox.setInternalState(syncTask, "previousSaturationLevels", previousSaturationLevels);
        Whitebox.setInternalState(syncTask, "previousExhaustionLevels", previousExhaustionLevels);

        assertEquals(1, previousSaturationLevels.size());
        assertEquals(1, previousExhaustionLevels.size());
        syncTask.onPlayerLogOut(player);
        assertEquals(0, previousSaturationLevels.size());
        assertEquals(0, previousExhaustionLevels.size());
    }

}
