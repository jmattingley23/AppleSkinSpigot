package com.jmatt.appleskinspigot;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

@PrepareForTest(AppleSkinSpigotPlugin.class)
@RunWith(PowerMockRunner.class)
public class AppleSkinSpigotPluginTest {

    @Mock
    SyncTask syncTaskMock;
    @Mock
    Server serverMock;
    @Mock
    Messenger messengerMock;
    @Mock
    PluginManager pluginManagerMock;

    AppleSkinSpigotPlugin plugin;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.plugin = Whitebox.newInstance(AppleSkinSpigotPlugin.class);
    }

    @Test
    public void onEnableEnablesPlugin() {
        plugin = spy(plugin);
        when(plugin.getServer()).thenReturn(serverMock);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        when(serverMock.getMessenger()).thenReturn(messengerMock);
        doReturn(syncTaskMock).when(plugin).createSyncTask();
        plugin.onEnable();
        Mockito.verify(pluginManagerMock, times(1)).registerEvents(any(LoginListener.class), eq(plugin));
        Mockito.verify(syncTaskMock, times(1)).runTaskTimer(eq(plugin), anyLong(), anyLong());
    }

    @Test
    public void createSyncTaskCreatesTask() throws Exception {
        syncTaskMock = createMock(SyncTask.class);
        expectNew(SyncTask.class, plugin).andReturn(syncTaskMock);

        replay(syncTaskMock, SyncTask.class);

        SyncTask result = plugin.createSyncTask();
        verify(syncTaskMock, SyncTask.class);
        assertEquals(result, syncTaskMock);
    }

    @Test
    public void onDisableDisablesPluginCancelsTask() {
        Whitebox.setInternalState(plugin, "syncTask", syncTaskMock);
        plugin.onDisable();
        Mockito.verify(syncTaskMock, times(1)).cancel();
    }

}
