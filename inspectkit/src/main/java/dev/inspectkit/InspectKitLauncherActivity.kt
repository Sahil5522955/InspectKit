package dev.inspectkit

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Dedicated launcher entrypoint. Uses an explicit Intent + MULTIPLE_TASK to avoid
 * launchers/search UIs resuming the host app's main task.
 */
class InspectKitLauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, InspectKitActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }
}

