package com.dipesh.ai

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class DipeshAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { active = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }
    override fun onInterrupt() { }
    override fun onDestroy() {
        if (active === this) active = null
        super.onDestroy()
    }

    companion object {
        @Volatile private var active: DipeshAccessibilityService? = null
        fun isEnabled(): Boolean = active != null
        fun runUserAction(action: String): Boolean {
            val service = active ?: return false
            val globalAction = when (action) {
                "back" -> GLOBAL_ACTION_BACK
                "home" -> GLOBAL_ACTION_HOME
                "notifications" -> GLOBAL_ACTION_NOTIFICATIONS
                "quick_settings" -> GLOBAL_ACTION_QUICK_SETTINGS
                else -> return false
            }
            return service.performGlobalAction(globalAction)
        }
    }
}
