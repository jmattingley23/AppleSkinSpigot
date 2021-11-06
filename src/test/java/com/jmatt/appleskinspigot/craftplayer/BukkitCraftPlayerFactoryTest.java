package com.jmatt.appleskinspigot.craftplayer;

import org.bukkit.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.logging.Logger;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BukkitCraftPlayerFactoryTest {

    @Mock
    Server serverMock;
    @Mock
    Logger loggerMock;

    @Test
    public void getBukkitCraftPlayer1_17_R1ReturnsPlayer() {
        when(serverMock.getBukkitVersion()).thenReturn("1.17.1-R0.1-SNAPSHOT");
        BukkitCraftPlayer bukkitCraftPlayer = new BukkitCraftPlayerFactory().getBukkitCraftPlayer(serverMock);
        assertTrue(bukkitCraftPlayer instanceof BukkitCraftPlayer_1_17_1);
    }

    @Test
    public void getBukkitCraftPlayerUnsupportedVersionReturnsNull() {
        when(serverMock.getBukkitVersion()).thenReturn("1.6.2");
        when(serverMock.getLogger()).thenReturn(loggerMock);
        BukkitCraftPlayer bukkitCraftPlayer = new BukkitCraftPlayerFactory().getBukkitCraftPlayer(serverMock);
        assertNull(bukkitCraftPlayer);
    }

}
