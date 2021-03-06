package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ConnectListener extends AbstractSkinListener {

    public ConnectListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPostLogin(LoginEvent loginEvent) {
        if (loginEvent.isCancelled() || isBlacklistEnabled()) {
            return;
        }

        PendingConnection connection = loginEvent.getConnection();
        String playerName = connection.getName().toLowerCase();

        loginEvent.registerIntent(plugin);
        Runnable task = () -> loadProfile(loginEvent, connection, playerName);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PostLoginEvent postLoginEvent) {
        ProxiedPlayer player = postLoginEvent.getPlayer();

        UserPreference preferences = plugin.getLoginSession(player.getPendingConnection());
        if (preferences == null || isBlacklistEnabled()) {
            return;
        }

        preferences.getTargetSkin().ifPresent(skin -> plugin.getApi().applySkin(player, skin));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        PendingConnection pendingConnection = disconnectEvent.getPlayer().getPendingConnection();
        UserPreference preference = plugin.endSession(pendingConnection);

        if (preference != null) {
            save(preference);
        }
    }

    private void loadProfile(AsyncEvent<?> loginEvent, PendingConnection conn, String playerName) {
        try {
            UserPreference preferences = initializeProfile(conn.getUniqueId(), playerName);
            plugin.startSession(conn, preferences);
        } finally {
            loginEvent.completeIntent(plugin);
        }
    }
}
