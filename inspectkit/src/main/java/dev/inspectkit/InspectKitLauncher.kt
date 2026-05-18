package dev.inspectkit

import android.content.Context
import android.content.Intent

object InspectKitLauncher {
    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, InspectKitActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

