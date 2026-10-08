package org.kde.kdeconnect.plugins.devkeys;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SpecialButtonState {

    boolean isCreated = false;
    boolean isActive = false;
    boolean isLocked = false;

    List<MaterialButton> buttons = new ArrayList<>();
    ExtraKeysView mExtraKeysView;

    public SpecialButtonState(ExtraKeysView extraKeysView) {
        mExtraKeysView = extraKeysView;
    }

    public void setIsCreated(boolean value) {
        isCreated = value;
    }

    public void setIsActive(boolean value) {
        isActive = value;
        for (MaterialButton button : buttons) {
            button.setTextColor(value ? mExtraKeysView.getButtonActiveTextColor() : mExtraKeysView.getButtonTextColor());
        }
    }

    public void setIsLocked(boolean value) {
        isLocked = value;
    }
}
