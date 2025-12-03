package com.dsatracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

/**
 * Helper class for opening URLs in Chrome Custom Tabs
 * Provides a seamless in-app browsing experience
 */
object ChromeTabsHelper {

    /**
     * Opens a URL in Chrome Custom Tabs with custom styling
     * Falls back to default browser if Chrome Custom Tabs is not available
     *
     * @param context Android context
     * @param url URL to open
     * @param toolbarColor Primary color for the toolbar (optional)
     */
    fun openUrl(
        context: Context,
        url: String,
        toolbarColor: Int? = null
    ) {
        try {
            val uri = Uri.parse(url)

            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .apply {
                    toolbarColor?.let { color ->
                        val colorScheme = CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(color)
                            .build()
                        setDefaultColorSchemeParams(colorScheme)
                    }
                }
                .build()

            // Launch the custom tab
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            // Fallback to default browser
            openInDefaultBrowser(context, url)
        }
    }

    /**
     * Opens URL in the device's default browser
     * Used as a fallback if Custom Tabs is not available
     */
    private fun openInDefaultBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ChromeTabsHelper", "Failed to open URL: $url", e)
        }
    }

    /**
     * Checks if Chrome Custom Tabs is available on the device
     */
    fun isCustomTabsAvailable(context: Context): Boolean {
        return try {
            val packageName = CustomTabsIntent.getPackageName(context, null)
            packageName != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens a problem URL with platform-specific handling
     *
     * @param context Android context
     * @param url Problem URL
     * @param platform Platform name (LeetCode, GFG, etc.)
     * @param toolbarColor Toolbar color
     */
    fun openProblemUrl(
        context: Context,
        url: String,
        platform: String,
        toolbarColor: Int? = null
    ) {
        android.util.Log.d("ChromeTabsHelper", "Opening $platform problem: $url")
        openUrl(context, url, toolbarColor)
    }
}

/**
 * Composable helper to get themed Chrome Custom Tabs color
 */
@Composable
fun rememberCustomTabsColor(): Int {
    val primaryColor = MaterialTheme.colorScheme.primary
    return primaryColor.toArgb()
}

/**
 * Extension function to open URL from String
 */
fun String.openInCustomTabs(
    context: Context,
    toolbarColor: Int? = null
) {
    ChromeTabsHelper.openUrl(context, this, toolbarColor)
}
