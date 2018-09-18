package pw.ipex.plex.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlexCorePersistentPlayerManager {
    public Map<String, String> updatedFieldUUID = new HashMap<>();
    public Map<String, String> updatedFieldName = new HashMap<>();

    public String updatePlayerData(String name) {
        String field1 = null;
        String field2 = null;
        int field1Type = 0; // 0 = uuid 1 = name
        String[] seperation = name.split(":");
        if (seperation.length == 1) {
            if (name.length() == 32 || name.length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(name);
            }
            else {
                field1 = name;
                field1Type = 1;
            }
        }
        else if (seperation.length == 2) {
            if (seperation[0].length() == 32 || seperation[0].length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(seperation[0]);
                field2 = seperation[1];
            }
            else {
                field1 = seperation[0];
                field2 = PlexCoreUtils.toFormattedUUID(seperation[1]);
                field1Type = 1;
            }
        }

        if (field1 == null) {
            return null;
        }

        Map<String, String> updatedField;

        if (field1Type == 0) {
            updatedField = this.updatedFieldUUID;
        }
        else {
            updatedField = this.updatedFieldName;
        }

        if (field2 == null || !updatedField.containsKey(field1)) {
            String newField2;
            if (field1Type == 0) { // uuid so get name
                newField2 = PlexCoreUtils.getName(field1);
            }
            else { // name so get uuid
                newField2 = PlexCoreUtils.getUUID(field1);
            }
            if (field2 != null) {
                updatedField.put(field1, newField2);
            }
        }

        if (updatedField.containsKey(field1)) {
            field2 = updatedField.get(field1);
        }

        if (field2 == null) {
            return field1;
        }
        return field1 + ":" + field2;
    }

    public boolean isPlayerDataUpdated(String name) {
        String field1 = null;
        int field1Type = 0; // 0 = uuid 1 = name
        String[] seperation = name.split(":");
        if (seperation.length == 1) {
            if (name.length() == 32 || name.length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(name);
            }
            else {
                field1 = name;
                field1Type = 1;
            }
        }
        else if (seperation.length == 2) {
            if (seperation[0].length() == 32 || seperation[0].length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(seperation[0]);
            }
            else {
                field1 = seperation[0];
                field1Type = 1;
            }
        }
        if (field1Type == 0) {
            return this.updatedFieldUUID.containsKey(field1);
        }
        return this.updatedFieldName.containsKey(field1);
    }

    public String[] extractPlayerData(String name) {
        String field1 = null;
        String field2 = null;
        int field1Type = 0; // 0 = uuid 1 = name
        String[] seperation = name.split(":");
        if (seperation.length == 1) {
            if (name.length() == 32 || name.length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(name);
            } else {
                field1 = name;
                field1Type = 1;
            }
        } else if (seperation.length == 2) {
            if (seperation[0].length() == 32 || seperation[0].length() == 36) {
                field1 = PlexCoreUtils.toFormattedUUID(seperation[0]);
                field2 = seperation[1];
            }
            else {
                field1 = seperation[0];
                field2 = PlexCoreUtils.toFormattedUUID(seperation[1]);
                field1Type = 1;
            }
        }

        if (field1Type == 0) {
            return new String[] {field1, field2};
        }
        else {
            return new String[] {field2, field1};
        }
    }

    public String getPlayerNameFromData(String name) {
        return this.extractPlayerData(name)[1];
    }

    public String getPlayerUUIDFromData(String name) {
        return this.extractPlayerData(name)[0];
    }
}
