package com.example.doomscroller.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: Drawable? = null
)

class AppCatalog(private val context: Context) {
    @Volatile
    private var cachedApps: List<InstalledApp>? = null

    fun getLaunchableApps(forceRefresh: Boolean = false): List<InstalledApp> {
        cachedApps?.let {
            if (!forceRefresh) return it
        }

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val apps = resolveInfoList
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null // Exclude ourselves

                val name = resolveInfo.loadLabel(pm).toString()
                InstalledApp(packageName, name, null)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }

        cachedApps = apps
        return apps
    }
}
