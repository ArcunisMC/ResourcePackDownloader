package com.arcunis.resourcepack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;

public final class Main extends JavaPlugin implements Listener {

    private static final String RESOURCE_PACK_URL = "https://github.com/ArcunisMC/Pack/releases/latest/download/pack.zip";
    private String resourcePackHash = null;

    @Override
    public void onEnable() {
        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Fetch the resource pack hash during plugin initialization
        try {
            this.resourcePackHash = fetchResourcePackHash();
            getLogger().info("Resource pack hash calculated: " + resourcePackHash);
        } catch (Exception e) {
            getLogger().severe("Failed to fetch or hash the resource pack: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (resourcePackHash != null) {
            // Send the resource pack to the player
            player.setResourcePack(RESOURCE_PACK_URL, resourcePackHash);
        } else {
            player.sendMessage("Resource pack is unavailable at the moment. Please contact the server administrator.");
        }
    }

    /**
     * Fetches the resource pack file from the URL and calculates its SHA-1 hash.
     *
     * @return The SHA-1 hash of the resource pack.
     * @throws Exception if an error occurs while fetching or hashing the file.
     */
    private String fetchResourcePackHash() throws Exception {
        // Open a connection to the resource pack URL
        InputStream inputStream = getInputStream();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        inputStream.close();

        // Convert the hash to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest.digest()) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private static InputStream getInputStream() throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(RESOURCE_PACK_URL).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // Check if the connection was successful
        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed to download resource pack: HTTP " + connection.getResponseCode());
        }

        // Read the file content
        return connection.getInputStream();
    }
}