package com.jmatt.appleskinspigot;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

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
import static org.mockito.Mockito.doReturn;

@PrepareForTest(SyncTask.class)
@RunWith(PowerMockRunner.class)
public class SyncTaskTest {

    @Mock
    AppleSkinSpigotPlugin pluginMock;
    @Mock
    Server serverMock;

    float saturation = 5F;
    float exhaustion = 1.8F;
    Player playerMock;
    UUID playerId;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(pluginMock.getServer()).thenReturn(serverMock);

        playerMock = mock(Player.class);
        when(playerMock.getSaturation()).thenReturn(saturation);
        when(playerMock.getExhaustion()).thenReturn(exhaustion);

        playerId = UUID.randomUUID();
        when(playerMock.getUniqueId()).thenReturn(playerId);
    }

    @Test
    public void updatePlayerSendsPacket() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(playerMock, atLeastOnce()).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.SATURATION_KEY), any());
        verify(playerMock, atLeastOnce()).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.EXHAUSTION_KEY), any());
    }

    @Test
    public void updatePlayerSkipsSaturationIfUnchanged() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));
        Map<UUID, Float> previousSaturationLevels = Whitebox.getInternalState(syncTask, "previousSaturationLevels");
        previousSaturationLevels.put(playerId, saturation);

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(playerMock, never()).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.SATURATION_KEY), any());
        verify(playerMock, times(1)).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.EXHAUSTION_KEY), any());
    }

    @Test
    public void updatePlayerSkipsExhaustionIfBelowThreshold() throws Exception {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));
        Map<UUID, Float> previousExhaustionLevels = Whitebox.getInternalState(syncTask, "previousExhaustionLevels");
        previousExhaustionLevels.put(playerId, exhaustion-.001F);

        Whitebox.invokeMethod(syncTask, "updatePlayer", playerMock);

        verify(playerMock, times(1)).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.SATURATION_KEY), any());
        verify(playerMock, never()).sendPluginMessage(eq(pluginMock), eq(AppleSkinSpigotPlugin.EXHAUSTION_KEY), any());
    }

    @Test
    public void runUpdatesAllOnlinePlayers() throws Exception {
        Player firstPlayer = Whitebox.newInstance(Player.class);
        Player secondPlayer = Whitebox.newInstance(Player.class);
        List<Player> players = List.of(firstPlayer, secondPlayer);
        doReturn(players).when(serverMock).getOnlinePlayers();

        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));
        PowerMockito.doNothing().when(syncTask, "updatePlayer", any(Player.class));
        syncTask.run();
        PowerMockito.verifyPrivate(syncTask, times(players.size())).invoke("updatePlayer", any(Player.class));
    }

    @Test
    public void playerLogInRemovesPreviousLevels() {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));
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
    public void playerLogOutRemovesPreviousLevels() {
        SyncTask syncTask = PowerMockito.spy(new SyncTask(pluginMock));
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
