package org.kde.kdeconnect.plugins.devkeys;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class SpecialButton {

    private static final HashMap<String, SpecialButton> map = new HashMap<>();

    public static final SpecialButton CTRL = new SpecialButton("CTRL");
    public static final SpecialButton ALT = new SpecialButton("ALT");
    public static final SpecialButton SHIFT = new SpecialButton("SHIFT");
    public static final SpecialButton FN = new SpecialButton("FN");

    private final String key;

    public SpecialButton(@NonNull final String key) {
        this.key = key;
        map.put(key, this);
    }

    public String getKey() {
        return key;
    }

    public static SpecialButton valueOf(String key) {
        return map.get(key);
    }

    @NonNull
    @Override
    public String toString() {
        return key;
    }
}
