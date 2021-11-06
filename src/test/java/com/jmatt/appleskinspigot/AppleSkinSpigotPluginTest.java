package com.jmatt.appleskinspigot;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.jmatt.appleskinspigot.craftplayer.BukkitCraftPlayerFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.api.easymock.PowerMock.replay;

@PrepareForTest(AppleSkinSpigotPlugin.class)
@RunWith(PowerMockRunner.class)
public class AppleSkinSpigotPluginTest {

    @Mock
    SyncTask syncTaskMock;
    @Mock
    Server serverMock;
    @Mock
    PluginManager pluginManagerMock;

    AppleSkinSpigotPlugin plugin;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.plugin = Whitebox.newInstance(AppleSkinSpigotPlugin.class);
    }

    @Test
    public void onEnableEnablesPlugin() throws UnsupportedMinecraftVersionException {
        plugin = spy(plugin);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        doReturn(syncTaskMock).when(plugin).createSyncTask();
        plugin.onEnable();
        verify(pluginManagerMock, times(1)).registerEvents(any(LoginListener.class), eq(plugin));
        verify(syncTaskMock, times(1)).runTaskTimer(eq(plugin), anyLong(), anyLong());
    }

    @Test
    public void onEnableWithExceptionDisablesPlugin() throws UnsupportedMinecraftVersionException {
        plugin = spy(plugin);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        doThrow(UnsupportedMinecraftVersionException.class).when(plugin).createSyncTask();
        plugin.onEnable();
        verify(pluginManagerMock, times(1)).disablePlugin(plugin);
    }

    @Test
    public void createSyncTaskCreatesTask() throws Exception {
        syncTaskMock = createMock(SyncTask.class);
        BukkitCraftPlayerFactory factoryMock = createMock(BukkitCraftPlayerFactory.class);
        expectNew(BukkitCraftPlayerFactory.class).andReturn(factoryMock);
        expectNew(SyncTask.class, plugin, factoryMock).andReturn(syncTaskMock);

        replay(factoryMock, BukkitCraftPlayerFactory.class);
        replay(syncTaskMock, SyncTask.class);

        SyncTask result = plugin.createSyncTask();
        verify(factoryMock, BukkitCraftPlayerFactory.class);
        verify(syncTaskMock, SyncTask.class);
        assertEquals(result, syncTaskMock);
    }

    @Test
    public void onDisableDisablesPluginCancelsTask() {
        Whitebox.setInternalState(plugin, "syncTask", syncTaskMock);
        plugin.onDisable();
        verify(syncTaskMock, times(1)).cancel();
    }

}
