package com.codeforcesvisualizer.core.ratingalerts

import kotlin.math.abs

/** How often the background job looks for a new rating; platforms may run it later. */
const val RATING_CHECK_INTERVAL_HOURS = 3

/** A rating change the user hasn't been told about yet. */
data class RatingChangeAlert(
    val handle: String,
    val contestId: Int,
    val contestName: String,
    val oldRating: Int,
    val newRating: Int,
    val rank: Int,
) {
    val delta: Int get() = newRating - oldRating

    /** For example "tourist: +57". */
    val title: String get() = "$handle: ${if (delta >= 0) "+" else "-"}${abs(delta)}"

    /** For example "Codeforces Round 1000: 3800 → 3857, rank 5". */
    val message: String get() = "$contestName: $oldRating → $newRating, rank $rank"
}

enum class RatingCheckResult {
    /** Alerts are off or no handle is saved. */
    Skipped,

    /** The ratings couldn't be loaded; worth retrying. */
    Failed,

    /** First check for this handle: the latest contest was recorded without an alert. */
    Baseline,
    NoChange,
    Notified,
}

/** Shows a rating change notification. */
interface RatingChangeNotifier {
    fun show(alert: RatingChangeAlert)
}

/** Runs [RatingChangeAlerts.check] periodically in the background while alerts are on. */
interface RatingCheckScheduler {
    fun setEnabled(enabled: Boolean)
}
