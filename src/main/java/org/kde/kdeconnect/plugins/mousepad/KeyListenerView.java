/*
 * SPDX-FileCopyrightText: 2014 Saikrishna Arcot <saiarcot895@gmail.com>
 *
 * SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL
*/

package org.kde.kdeconnect.plugins.mousepad;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.kde.kdeconnect.KdeConnect;
import org.kde.kdeconnect.NetworkPacket;
import org.kde.kdeconnect.plugins.devkeys.ExtraKeysView;
import org.kde.kdeconnect.plugins.devkeys.SpecialButton;

import java.util.Locale;

public class KeyListenerView extends View {

    private String deviceId;
    private ExtraKeysView developerKeysView;

    public static final SparseIntArray SpecialKeysMap = new SparseIntArray();

    static {
        int i = 0;
        SpecialKeysMap.put(KeyEvent.KEYCODE_DEL, ++i);              // 1
        SpecialKeysMap.put(KeyEvent.KEYCODE_TAB, ++i);              // 2
        SpecialKeysMap.put(KeyEvent.KEYCODE_ENTER, 12);
        ++i;        // 3 is not used, return is 12 instead
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_LEFT, ++i);        // 4
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_UP, ++i);          // 5
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, ++i);       // 6
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_DOWN, ++i);        // 7
        SpecialKeysMap.put(KeyEvent.KEYCODE_PAGE_UP, ++i);          // 8
        SpecialKeysMap.put(KeyEvent.KEYCODE_PAGE_DOWN, ++i);        // 9
        SpecialKeysMap.put(KeyEvent.KEYCODE_MOVE_HOME, ++i);    // 10
        SpecialKeysMap.put(KeyEvent.KEYCODE_MOVE_END, ++i);     // 11
        SpecialKeysMap.put(KeyEvent.KEYCODE_NUMPAD_ENTER, ++i); // 12
        SpecialKeysMap.put(KeyEvent.KEYCODE_FORWARD_DEL, ++i);  // 13
        SpecialKeysMap.put(KeyEvent.KEYCODE_ESCAPE, ++i);       // 14
        SpecialKeysMap.put(KeyEvent.KEYCODE_SYSRQ, ++i);        // 15
        SpecialKeysMap.put(KeyEvent.KEYCODE_SCROLL_LOCK, ++i);  // 16
        SpecialKeysMap.put(KeyEvent.KEYCODE_CTRL_LEFT, ++i);    // 17
        SpecialKeysMap.put(KeyEvent.KEYCODE_ALT_LEFT, ++i);     // 18
        SpecialKeysMap.put(KeyEvent.KEYCODE_SHIFT_LEFT, ++i);   // 19
        SpecialKeysMap.put(KeyEvent.KEYCODE_META_LEFT, ++i);    // 20
        SpecialKeysMap.put(KeyEvent.KEYCODE_F1, ++i);           // 21
        SpecialKeysMap.put(KeyEvent.KEYCODE_F2, ++i);           // 22
        SpecialKeysMap.put(KeyEvent.KEYCODE_F3, ++i);           // 23
        SpecialKeysMap.put(KeyEvent.KEYCODE_F4, ++i);           // 24
        SpecialKeysMap.put(KeyEvent.KEYCODE_F5, ++i);           // 25
        SpecialKeysMap.put(KeyEvent.KEYCODE_F6, ++i);           // 26
        SpecialKeysMap.put(KeyEvent.KEYCODE_F7, ++i);           // 27
        SpecialKeysMap.put(KeyEvent.KEYCODE_F8, ++i);           // 28
        SpecialKeysMap.put(KeyEvent.KEYCODE_F9, ++i);           // 29
        SpecialKeysMap.put(KeyEvent.KEYCODE_F10, ++i);          // 30
        SpecialKeysMap.put(KeyEvent.KEYCODE_F11, ++i);          // 31
        SpecialKeysMap.put(KeyEvent.KEYCODE_F12, ++i);          // 21
    }

    public void setDeviceId(String id) {
        deviceId = id;
    }

    public void setDeveloperKeysView(ExtraKeysView view) {
        developerKeysView = view;
    }

    public KeyListenerView(Context context, AttributeSet set) {
        super(context, set);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        return new KeyInputConnection(this, true);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    public void sendChars(CharSequence chars) {
        if (chars.length() == 0) {
            return;
        }

        DeveloperModifiers modifiers = consumeDeveloperModifiers();
        if (modifiers.hasModifiers()) {
            for (int offset = 0; offset < chars.length();) {
                int codePoint = Character.codePointAt(chars, offset);
                NetworkPacket np = new NetworkPacket(MousePadPlugin.PACKET_TYPE_MOUSEPAD_REQUEST);
                addModifiers(np, modifiers.alt, modifiers.ctrl, modifiers.shift, false);
                String key = codePoint >= 1 && codePoint <= 26 && modifiers.ctrl
                    ? String.valueOf((char) ('a' + codePoint - 1))
                    : new String(Character.toChars(codePoint)).toLowerCase(Locale.ROOT);
                np.set("key", key);
                sendKeyPressPacket(np);
                offset += Character.charCount(codePoint);
            }
            return;
        }

        MousePadPlugin plugin = KdeConnect.getInstance().getDevicePlugin(deviceId, MousePadPlugin.class);
        if (plugin == null) {
            return;
        }
        plugin.sendText(chars.toString());
    }

    private void sendKeyPressPacket(final NetworkPacket np) {
        MousePadPlugin plugin = KdeConnect.getInstance().getDevicePlugin(deviceId, MousePadPlugin.class);
        if (plugin == null) {
            return;
        }
        plugin.sendPacket(np);
    }

    private DeveloperModifiers consumeDeveloperModifiers() {
        if (developerKeysView == null) {
            return DeveloperModifiers.NONE;
        }

        return new DeveloperModifiers(
            Boolean.TRUE.equals(developerKeysView.readSpecialButton(SpecialButton.ALT, true)),
            Boolean.TRUE.equals(developerKeysView.readSpecialButton(SpecialButton.CTRL, true)),
            Boolean.TRUE.equals(developerKeysView.readSpecialButton(SpecialButton.SHIFT, true)));
    }

    private static void addModifiers(NetworkPacket np, boolean alt, boolean ctrl, boolean shift, boolean meta) {
        if (alt) np.set("alt", true);
        if (ctrl) np.set("ctrl", true);
        if (shift) np.set("shift", true);
        if (meta) np.set("super", true);
    }

    private static final class DeveloperModifiers {
        private static final DeveloperModifiers NONE = new DeveloperModifiers(false, false, false);
        private final boolean alt;
        private final boolean ctrl;
        private final boolean shift;

        private DeveloperModifiers(boolean alt, boolean ctrl, boolean shift) {
            this.alt = alt;
            this.ctrl = ctrl;
            this.shift = shift;
        }

        private boolean hasModifiers() {
            return alt || ctrl || shift;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // consume events that otherwise would move the focus away from us
        return keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
            keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
            keyCode == KeyEvent.KEYCODE_ENTER;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //We don't want to swallow the back button press
            return false;
        }

        // NOTE: Most keyboards, and specifically the Android default keyboard when
        // entering non-ascii characters, will not trigger KeyEvent events as documented
        // here: http://developer.android.com/reference/android/view/KeyEvent.html

        //Log.e("KeyDown", "------------");
        //Log.e("KeyDown", "keyChar:" + (int) event.getDisplayLabel());
        //Log.e("KeyDown", "utfChar:" + (char)event.getUnicodeChar());
        //Log.e("KeyDown", "intUtfChar:" + event.getUnicodeChar());

        final NetworkPacket np = new NetworkPacket(MousePadPlugin.PACKET_TYPE_MOUSEPAD_REQUEST);
        DeveloperModifiers developerModifiers = consumeDeveloperModifiers();

        boolean alt = event.isAltPressed() || developerModifiers.alt;
        boolean ctrl = event.isCtrlPressed() || developerModifiers.ctrl;
        boolean shift = event.isShiftPressed() || developerModifiers.shift;
        boolean meta = event.isMetaPressed();
        addModifiers(np, alt, ctrl, shift, meta);
        boolean modifier = alt || ctrl || developerModifiers.shift || meta;

        int specialKey = SpecialKeysMap.get(keyCode, -1);

        if (specialKey != -1) {
            np.set("specialKey", specialKey);
        } else if (event.getDisplayLabel() != 0 && modifier) {
            //Alt will change the utf symbol to non-ascii characters, we want the plain original letter
            //Since getDisplayLabel will always have a value, we have to check for special keys before
            char keyCharacter = event.getDisplayLabel();
            np.set("key", String.valueOf(keyCharacter).toLowerCase());
        } else {
            //A normal key, but still not handled by the KeyInputConnection (happens with numbers)
            np.set("key", String.valueOf((char) event.getUnicodeChar()));
        }

        sendKeyPressPacket(np);
        return true;

    }
}
