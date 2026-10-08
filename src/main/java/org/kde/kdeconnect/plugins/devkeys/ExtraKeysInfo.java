package org.kde.kdeconnect.plugins.devkeys;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtraKeysInfo {

    private final ExtraKeyButton[][] mButtons;

    public ExtraKeysInfo(@NonNull String propertiesInfo, String style,
                         @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap) throws JSONException {
        mButtons = initExtraKeysInfo(propertiesInfo, getCharDisplayMapForStyle(style), extraKeyAliasMap);
    }

    public ExtraKeysInfo(@NonNull String propertiesInfo,
                         @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap,
                         @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap) throws JSONException {
        mButtons = initExtraKeysInfo(propertiesInfo, extraKeyDisplayMap, extraKeyAliasMap);
    }

    private ExtraKeyButton[][] initExtraKeysInfo(@NonNull String propertiesInfo,
                                                 @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap,
                                                 @NonNull ExtraKeysConstants.ExtraKeyDisplayMap extraKeyAliasMap) throws JSONException {
        JSONArray arr = new JSONArray(propertiesInfo);
        Object[][] matrix = new Object[arr.length()][];
        for (int i = 0; i < arr.length(); i++) {
            JSONArray line = arr.getJSONArray(i);
            matrix[i] = new Object[line.length()];
            for (int j = 0; j < line.length(); j++) {
                matrix[i][j] = line.get(j);
            }
        }

        ExtraKeyButton[][] buttons = new ExtraKeyButton[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            buttons[i] = new ExtraKeyButton[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                Object key = matrix[i][j];
                JSONObject jobject = normalizeKeyConfig(key);
                ExtraKeyButton button;

                if (!jobject.has(ExtraKeyButton.KEY_POPUP)) {
                    button = new ExtraKeyButton(jobject, extraKeyDisplayMap, extraKeyAliasMap);
                } else {
                    JSONObject popupJobject = normalizeKeyConfig(jobject.get(ExtraKeyButton.KEY_POPUP));
                    ExtraKeyButton popup = new ExtraKeyButton(popupJobject, extraKeyDisplayMap, extraKeyAliasMap);
                    button = new ExtraKeyButton(jobject, popup, extraKeyDisplayMap, extraKeyAliasMap);
                }

                buttons[i][j] = button;
            }
        }

        return buttons;
    }

    private static JSONObject normalizeKeyConfig(Object key) throws JSONException {
        JSONObject jobject;
        if (key instanceof String) {
            jobject = new JSONObject();
            jobject.put(ExtraKeyButton.KEY_KEY_NAME, key);
        } else if (key instanceof JSONObject) {
            jobject = (JSONObject) key;
        } else {
            throw new JSONException("An key in the extra-key matrix must be a string or an object");
        }
        return jobject;
    }

    public ExtraKeyButton[][] getMatrix() {
        return mButtons;
    }

    @NonNull
    public static ExtraKeysConstants.ExtraKeyDisplayMap getCharDisplayMapForStyle(String style) {
        switch (style) {
            case "arrows-only":
                return ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.ARROWS_ONLY_CHAR_DISPLAY;
            case "arrows-all":
                return ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.LOTS_OF_ARROWS_CHAR_DISPLAY;
            case "all":
                return ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.FULL_ISO_CHAR_DISPLAY;
            case "none":
                return new ExtraKeysConstants.ExtraKeyDisplayMap();
            default:
                return ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY;
        }
    }
}
