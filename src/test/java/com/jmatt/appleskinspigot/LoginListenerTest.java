package com.jmatt.appleskinspigot;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginListenerTest {

    @Mock
    SyncTask syncTaskMock;
    @Mock
    Player playerMock;
    @Mock
    PlayerJoinEvent playerJoinEventMock;
    @Mock
    PlayerQuitEvent playerQuitEventMock;

    @Test
    public void onPlayerJoinInvokesMethod() {
        when(playerJoinEventMock.getPlayer()).thenReturn(playerMock);
        new LoginListener(syncTaskMock).onPlayerJoin(playerJoinEventMock);
        verify(syncTaskMock, times(1)).onPlayerLogIn(playerMock);
    }

    @Test
    public void onPlayerLeaveInvokesMethod() {
        when(playerQuitEventMock.getPlayer()).thenReturn(playerMock);
        new LoginListener(syncTaskMock).onPlayerLeave(playerQuitEventMock);
        verify(syncTaskMock, times(1)).onPlayerLogOut(playerMock);
    }

}
