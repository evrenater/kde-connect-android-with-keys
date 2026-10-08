package org.kde.kdeconnect.plugins.devkeys;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExtraKeyButton {

    public static final String KEY_KEY_NAME = "key";
    public static final String KEY_MACRO = "macro";
    public static final String KEY_DISPLAY_NAME = "display";
    public static final String KEY_POPUP = "popup";

    private final String key;
    private final boolean macro;
    private final String display;

    @Nullable
    private final ExtraKeyButton popup;

    public ExtraKeyButton(@NonNull JSONObject config,
                          @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap,
                          @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap) throws JSONException {
        this(config, null, extraKeyDisplayMap, extraKeyAliasMap);
    }

    public ExtraKeyButton(@NonNull JSONObject config, @Nullable ExtraKeyButton popup,
                          @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap,
                          @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap) throws JSONException {
        String keyFromConfig = getStringFromJson(config, KEY_KEY_NAME);
        String macroFromConfig = getStringFromJson(config, KEY_MACRO);
        String[] keys;
        if (keyFromConfig != null && macroFromConfig != null) {
            throw new JSONException("Both key and macro can't be set for the same key. key: \"" + keyFromConfig + "\", macro: \"" + macroFromConfig + "\"");
        } else if (keyFromConfig != null) {
            keys = new String[]{keyFromConfig};
            this.macro = false;
        } else if (macroFromConfig != null) {
            keys = macroFromConfig.split(" ");
            this.macro = true;
        } else {
            throw new JSONException("All keys have to specify either key or macro");
        }

        for (int i = 0; i < keys.length; i++) {
            keys[i] = replaceAlias(extraKeyAliasMap, keys[i]);
        }

        this.key = TextUtils.join(" ", keys);

        String displayFromConfig = getStringFromJson(config, KEY_DISPLAY_NAME);
        if (displayFromConfig != null) {
            this.display = displayFromConfig;
        } else {
            this.display = Arrays.stream(keys)
                .map(key -> extraKeyDisplayMap.get(key, key))
                .collect(Collectors.joining(" "));
        }

        this.popup = popup;
    }

    public String getStringFromJson(@NonNull JSONObject config, @NonNull String key) {
        try {
            return config.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public String getKey() {
        return key;
    }

    public boolean isMacro() {
        return macro;
    }

    public String getDisplay() {
        return display;
    }

    @Nullable
    public ExtraKeyButton getPopup() {
        return popup;
    }

    public static String replaceAlias(@NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap, String key) {
        return extraKeyAliasMap.get(key, key);
    }
}
