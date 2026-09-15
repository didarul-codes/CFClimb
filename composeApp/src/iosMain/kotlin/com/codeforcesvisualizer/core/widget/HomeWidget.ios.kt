package com.codeforcesvisualizer.core.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.codeforcesvisualizer.core.theme.rankTierFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/** Shared with the widget extension; must match the App Group in both entitlements files. */
private const val APP_GROUP = "group.com.codeforcesvisualizer.iosApp"

// Keys must match CFClimbWidget.swift.
private const val KEY_ROUND_NAME = "widget.nextRoundName"
private const val KEY_ROUND_START = "widget.nextRoundStart"
private const val KEY_HANDLE = "widget.handle"
private const val KEY_RATING = "widget.rating"
private const val KEY_TIER_NAME = "widget.tierName"
private const val KEY_TIER_COLOR = "widget.tierColor"

private var widgetReloader: (() -> Unit)? = null

/** Set from Swift at launch, because `WidgetCenter` can only be called from Swift. */
fun setWidgetReloader(reload: () -> Unit) {
    widgetReloader = reload
}

/**
 * Writes the widget snapshot to the App Group, where the widget extension reads it without
 * loading the Kotlin framework, then asks WidgetKit to redraw.
 */
class IosWidgetUpdater(private val dataSource: WidgetDataSource) : WidgetUpdater {
    override suspend fun update() {
        val snapshot = dataSource.snapshot()
        val defaults = NSUserDefaults(suiteName = APP_GROUP)

        val round = snapshot.nextRound
        if (round != null) {
            defaults.setObject(round.name, forKey = KEY_ROUND_NAME)
            defaults.setDouble(round.startTimeEpochSeconds.toDouble(), forKey = KEY_ROUND_START)
        } else {
            defaults.removeObjectForKey(KEY_ROUND_NAME)
            defaults.removeObjectForKey(KEY_ROUND_START)
        }

        val handle = snapshot.handle
        val rating = snapshot.rating
        if (handle != null && rating != null) {
            val tier = rankTierFor(rating)
            defaults.setObject(handle, forKey = KEY_HANDLE)
            defaults.setInteger(rating.toLong(), forKey = KEY_RATING)
            defaults.setObject(tier.name, forKey = KEY_TIER_NAME)
            defaults.setInteger(tier.color.toArgb().toLong(), forKey = KEY_TIER_COLOR)
        } else {
            listOf(KEY_HANDLE, KEY_RATING, KEY_TIER_NAME, KEY_TIER_COLOR).forEach(defaults::removeObjectForKey)
        }

        withContext(Dispatchers.Main) { widgetReloader?.invoke() }
    }
}

// iOS has no API for an app to add its own widget; people add widgets from the home screen.
@Composable
actual fun rememberAddWidgetAction(): (() -> Unit)? = null
