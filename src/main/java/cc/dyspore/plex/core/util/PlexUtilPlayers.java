package cc.dyspore.plex.core.util;

import cc.dyspore.plex.core.util.PlexUtilMojang;

import java.util.HashMap;
import java.util.Map;

public class PlexUtilPlayers {
    public static Map<String, String> uuidToName = new HashMap<>();

    public static String updatePlayerData(String playerData) {
        if (playerData.startsWith("!")) {
            playerData = playerData.substring(1);
        }
        String name = null;
        String uuid = null;
        String[] seperation = playerData.split(":");
        if (seperation.length == 1) {
            if (playerData.length() == 32 || playerData.length() == 36) {
                uuid = PlexUtilMojang.toFormattedUUID(playerData);
            }
            else {
                name = playerData;
            }
        }
        else if (seperation.length == 2) {
            uuid = PlexUtilMojang.toFormattedUUID(seperation[0]);
            name = seperation[1];
        }

        if (uuid == null && name == null) {
            return null;
        }

        if (uuid == null) {
            uuid = PlexUtilMojang.getUUID(name);
            if (uuid != null) {
                uuidToName.put(uuid, name);
            }
        }

        if (uuid != null && (name == null || !uuidToName.containsKey(uuid))) {
            String newName = PlexUtilMojang.getName(uuid);
            if (newName != null) {
                uuidToName.put(uuid, newName);
            }
        }

        boolean updatedData = false;
        if (uuid != null && uuidToName.containsKey(uuid)) {
            name = uuidToName.get(uuid);
            updatedData = true;
        }

        if (uuid == null) {
            return "!" + name;
        }
        if (name == null) {
            return uuid;
        }

        return (updatedData ? "" : "!") + uuid + ":" + name;
    }

    public static boolean isPlayerDataUpdated(String playerData) {
        if (playerData.startsWith("!")) {
            playerData = playerData.substring(1);
        }
        String uuid = null;
        String name = null;
        String[] seperation = playerData.split(":");
        if (seperation.length == 1) {
            if (playerData.length() == 32 || playerData.length() == 36) {
                uuid = PlexUtilMojang.toFormattedUUID(playerData);
            }
            else {
                name = playerData;
            }
        }
        else if (seperation.length == 2) {
            uuid = PlexUtilMojang.toFormattedUUID(seperation[0]);
            name = seperation[1];
        }
        return uuidToName.containsKey(uuid);
    }

    public static String[] extractPlayerData(String playerData) {
        if (playerData.startsWith("!")) {
            playerData = playerData.substring(1);
        }
        String uuid = null;
        String name = null;
        String[] seperation = playerData.split(":");
        if (seperation.length == 1) {
            if (playerData.length() == 32 || playerData.length() == 36) {
                uuid = PlexUtilMojang.toFormattedUUID(playerData);
            }
            else {
                name = playerData;
            }
        }
        else if (seperation.length == 2) {
            uuid = PlexUtilMojang.toFormattedUUID(seperation[0]);
            name = seperation[1];
        }
        return new String[] {uuid, name};
    }

    public static String getPlayerNameFromData(String name) {
        return extractPlayerData(name)[1];
    }

    public static String getPlayerUUIDFromData(String name) {
        return extractPlayerData(name)[0];
    }
}
