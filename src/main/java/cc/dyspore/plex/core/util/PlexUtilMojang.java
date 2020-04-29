package cc.dyspore.plex.core.util;

import cc.dyspore.plex.Plex;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlexUtilMojang {
    public static ConcurrentHashMap<String, String> nameToUuid = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> uuidToName = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> uuidLookupTimes = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> nameLookupTimes = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> skinLookupTimes = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ResourceLocation> uuidToTexture = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ResourceLocation> nameDefaultSkins = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ResourceLocation> uuidDefaultSkins = new ConcurrentHashMap<>();

    public static ResourceLocation getDefaultSkin() {
        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    public static ResourceLocation getDefaultSkin(String input) {
        return DefaultPlayerSkin.getDefaultSkin(UUID.nameUUIDFromBytes(input.getBytes()));
    }

    public static ResourceLocation getSkin(String player) {
        return getSkin(new GameProfile(null, player));
    }

    public static ResourceLocation profileDefaultSkin(GameProfile profile) {
        Boolean uuidNull = (profile.getId() == null);
        Boolean nameNull = (profile.getName() == null);
        if (nameNull && uuidNull) {
            return getDefaultSkin();
        }
        if (!uuidNull) {
            if (uuidDefaultSkins.containsKey(profile.getId().toString())) {
                return uuidDefaultSkins.get(profile.getId().toString());
            }
        }
        if (!nameNull) {
            if (nameDefaultSkins.containsKey(profile.getName().toLowerCase())) {
                return nameDefaultSkins.get(profile.getName().toLowerCase());
            }
        }
        if (!nameNull) {
            nameDefaultSkins.put(profile.getName().toLowerCase(), getDefaultSkin(profile.getName().toLowerCase()));
            return nameDefaultSkins.get(profile.getName().toLowerCase());
        }
        uuidDefaultSkins.put(profile.getId().toString(), getDefaultSkin(profile.getId().toString()));
        return uuidDefaultSkins.get(profile.getId().toString());
    }

    public static String getUUID(String ign) {
        return getUUID(ign, true);
    }

    public static String getUUID(String ign, boolean requestIfNeeded) {
        String friendlyIgn = ign.toLowerCase();
        if (nameToUuid.get(friendlyIgn) == null) {
            NetworkPlayerInfo playerInfo = Plex.minecraft.getNetHandler().getPlayerInfo(ign);
            if (playerInfo != null) {
                if (playerInfo.getGameProfile().getId() != null) {
                    nameToUuid.put(friendlyIgn, playerInfo.getGameProfile().getId().toString());
                    uuidToName.put(playerInfo.getGameProfile().getId().toString(), friendlyIgn);
                }
            }
        }
        if (nameToUuid.get(friendlyIgn) != null) {
            return nameToUuid.get(friendlyIgn);
        }
        uuidLookupTimes.putIfAbsent(friendlyIgn, 0L);
        if (Minecraft.getSystemTime() > uuidLookupTimes.get(friendlyIgn) + 30000L && requestIfNeeded) {
            uuidLookupTimes.put(friendlyIgn, Minecraft.getSystemTime());
            new Thread(() -> fetchUUID(ign)).start();
        }
        if (nameToUuid.containsKey(friendlyIgn)) {
            return nameToUuid.get(friendlyIgn);
        }
        return null;
    }

    public static String getName(String playerUuid) {
        return getName(playerUuid, true);
    }

    public static String getName(String playerUuid, boolean requestIfNeeded) {
        final String uuid = toFormattedUUID(playerUuid);
        if (uuid == null) {
            return null;
        }
        if (uuidToName.get(uuid) == null) {
            NetworkPlayerInfo playerInfo = Plex.minecraft.getNetHandler().getPlayerInfo(UUID.fromString(uuid));
            if (playerInfo != null) {
                if (playerInfo.getGameProfile().getName() != null) {
                    nameToUuid.put(playerInfo.getGameProfile().getName(), uuid);
                    uuidToName.put(uuid, playerInfo.getGameProfile().getName());
                }
            }
        }
        if (uuidToName.get(uuid) != null) {
            return nameToUuid.get(uuid);
        }
        nameLookupTimes.putIfAbsent(uuid, 0L);
        if (Minecraft.getSystemTime() > nameLookupTimes.get(uuid) + 30000L && requestIfNeeded) {
            nameLookupTimes.put(uuid, Minecraft.getSystemTime());
            new Thread(() -> fetchIGN(uuid)).start();
        }
        if (uuidToName.containsKey(uuid)) {
            return uuidToName.get(uuid);
        }
        return null;
    }

    public static ResourceLocation getSkin(final GameProfile player) {
        return getSkin(player, true);
    }

    public static ResourceLocation getSkin(final GameProfile player, boolean requestIfNeeded) {
        if (player == null) {
            return getDefaultSkin();
        }
        if (player.getId() == null && player.getName() == null) {
            return getDefaultSkin();
        }
        String playerUUID;
        if (player.getId() == null) {
            playerUUID = getUUID(player.getName().toLowerCase(), true);
        }
        else {
            playerUUID = player.getId().toString();
        }
        if (playerUUID == null) {
            return profileDefaultSkin(player);
        }
        if (uuidToTexture.containsKey(playerUUID)) {
            return uuidToTexture.get(playerUUID);
        }
        skinLookupTimes.putIfAbsent(playerUUID, 0L);
        if (Minecraft.getSystemTime() > skinLookupTimes.get(playerUUID) + 30000L && requestIfNeeded) {
            skinLookupTimes.put(playerUUID, Minecraft.getSystemTime());
            final GameProfile playerGameProfile = new GameProfile(UUID.fromString(playerUUID), player.getName());
            new Thread(() -> fetchSkin(playerGameProfile)).start();
        }
        if (uuidToTexture.containsKey(playerUUID)) {
            return uuidToTexture.get(playerUUID);
        }
        return profileDefaultSkin(player);
    }

    public static String fetchUUID(String ign) {
        if (nameToUuid.containsKey(ign)) {
            return nameToUuid.get(ign);
        }
        //PlexUtil.chatAddMessage("fetching uuid for " + ign);
        try {
            URL uuidAPI = new URL("https://api.mojang.com/users/profiles/minecraft/" + ign);
            InputStreamReader uuidInputReader = new InputStreamReader(uuidAPI.openStream());
            JsonReader jsonReader = new JsonReader(uuidInputReader);
            JsonParser jsonParser = new JsonParser();
            JsonElement apiResponse = jsonParser.parse(jsonReader);
            uuidInputReader.close();
            final String playerUUID = apiResponse.getAsJsonObject().get("id").getAsString();
            final String formattedPlayerUUID = toFormattedUUID(playerUUID);
            if (formattedPlayerUUID == null) {
                return null;
            }
            nameToUuid.put(ign.toLowerCase(), formattedPlayerUUID);
            uuidToName.put(formattedPlayerUUID, ign);
            return playerUUID;
        }
        catch (Throwable e) {
            Plex.logger.info("[PlexUtil] exception getting player uuid for " + ign);
            Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public static String fetchIGN(String uuid) {
        uuid = toFormattedUUID(uuid);
        if (uuid == null) {
            return null;
        }
        if (uuidToName.containsKey(uuid)) {
            return uuidToName.get(uuid);
        }
        //PlexUtil.chatAddMessage("fetching uuid for " + ign);
        try {
            URL nameAPI = new URL("https://api.mojang.com/user/profiles/" + toUnformattedUUID(uuid) + "/names");
            InputStreamReader uuidInputReader = new InputStreamReader(nameAPI.openStream());
            JsonReader jsonReader = new JsonReader(uuidInputReader);
            JsonParser jsonParser = new JsonParser();
            JsonElement apiResponse = jsonParser.parse(jsonReader);
            uuidInputReader.close();
            JsonArray names = apiResponse.getAsJsonArray();
            final String playerName = names.get(names.size() - 1).getAsJsonObject().get("name").getAsString();
            nameToUuid.put(playerName, uuid);
            uuidToName.put(uuid, playerName);
            return playerName;
        }
        catch (Throwable e) {
            Plex.logger.info("[PlexUtil] exception getting player name for " + uuid);
            Plex.logger.info(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public static void fetchSkin(final GameProfile player) {
        if (player.getId() == null) {
            return;
        }
        final String uuid = player.getId().toString();
        if (uuidToTexture.containsKey(uuid)) {
            return;
        }
        //PlexUtil.chatAddMessage("loading skin " + player.getId() + (player.getName() == null ? "" : " // " + player.getName()));
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> skinCache = Plex.minecraft.getSkinManager().loadSkinFromCache(player);
        if (skinCache.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            uuidToTexture.put(uuid, Plex.minecraft.getSkinManager().loadSkin(skinCache.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN));
            return;
        }

        GameProfile profile = Plex.minecraft.getSessionService().fillProfileProperties(player, true);

        if (profile.getName() != null) {
            if (!nameToUuid.containsKey(profile.getName().toLowerCase())) {
                nameToUuid.put(profile.getName().toLowerCase(), uuid);
            }
        }

        Plex.minecraft.getSkinManager().loadProfileTextures(profile, (type, resourceLocation, profileTexture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                uuidToTexture.put(player.getId().toString(), resourceLocation);
            }
        }, true);
    }

    public static String toFormattedUUID(String uuid) {
        if (uuid.length() == 32) {
            return uuid.substring(0, 8) + '-' + uuid.substring(8, 12) + '-' + uuid.substring(12, 16) + '-' + uuid.substring(16, 20) + '-' + uuid.substring(20, 32);
                   // 8 chars                   // 4 chars                    // 4 chars                     // 4 chars               // 12 chars
        }
        if (uuid.length() == 36) {
            return uuid;
        }
        return null;
    }

    public static String toUnformattedUUID(String uuid) {
        return uuid.replace("-", "");
    }
}
