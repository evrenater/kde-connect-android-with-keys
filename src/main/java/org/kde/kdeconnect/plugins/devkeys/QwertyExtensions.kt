package org.kde.kdeconnect.plugins.devkeys

import android.view.View
import com.google.android.material.button.MaterialButton

/**
 * Extension function to quickly setup [ExtraKeysView] in a Kotlin Android application
 * with a JSON config string and a click handling lambda.
 */
fun ExtraKeysView.setupExtraKeys(
    jsonConfig: String = "[['ESC','/',{key: '-', popup: '|'},'HOME','UP','END','PGUP'], ['TAB','CTRL','ALT','LEFT','DOWN','RIGHT','PGDN']]",
    style: String = "default",
    toolbarHeightPx: Float = 120f,
    onKeyClicked: (key: String, isMacro: Boolean) -> Unit
) {
    try {
        val extraKeysInfo = ExtraKeysInfo(
            jsonConfig,
            ExtraKeysInfo.getCharDisplayMapForStyle(style),
            ExtraKeysConstants.CONTROL_CHARS_ALIASES
        )

        setExtraKeysViewClient(object : ExtraKeysView.IExtraKeysView {
            override fun onExtraKeyButtonClick(view: View, buttonInfo: ExtraKeyButton, button: MaterialButton) {
                onKeyClicked(buttonInfo.key, buttonInfo.isMacro())
            }

            override fun performExtraKeyButtonHapticFeedback(view: View, buttonInfo: ExtraKeyButton, button: MaterialButton): Boolean {
                return false
            }
        })

        reload(extraKeysInfo, toolbarHeightPx)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
