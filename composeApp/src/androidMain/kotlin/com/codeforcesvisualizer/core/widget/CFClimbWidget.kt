package com.codeforcesvisualizer.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.codeforcesvisualizer.HomeActivity
import com.codeforcesvisualizer.core.theme.rankTierFor
import com.codeforcesvisualizer.core.utils.formatStartShort
import com.codeforcesvisualizer.core.utils.formatTimeUntil
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/** Next round and the saved handle's rating on the home screen. */
class CFClimbWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SmallSize, MediumSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = GlobalContext.get().get<WidgetDataSource>().snapshot()
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot, System.currentTimeMillis() / 1000)
            }
        }
    }

    companion object {
        val SmallSize = DpSize(110.dp, 48.dp)
        val MediumSize = DpSize(180.dp, 110.dp)
    }
}

class CFClimbWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CFClimbWidget()
}

@Composable
internal fun WidgetContent(snapshot: WidgetSnapshot, nowEpochSeconds: Long) {
    val showRating = LocalSize.current.height >= CFClimbWidget.MediumSize.height
    val round = snapshot.nextRound

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<HomeActivity>()),
    ) {
        Text(
            text = "cf://next round",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            ),
        )
        Spacer(GlanceModifier.height(4.dp))
        if (round == null) {
            Text(
                text = "No upcoming rounds saved. Open CFClimb to refresh.",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                maxLines = 2,
            )
        } else {
            Text(
                text = round.name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2,
            )
            Text(
                text = "in ${formatTimeUntil(round.startTimeEpochSeconds, nowEpochSeconds)} · " +
                    formatStartShort(round.startTimeEpochSeconds),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                maxLines = 1,
            )
        }

        val rating = snapshot.rating
        if (showRating && snapshot.handle != null) {
            // Pushes the rating line to the bottom of taller widgets.
            Spacer(GlanceModifier.defaultWeight())
            if (rating == null) {
                Text(
                    text = "${snapshot.handle} · open the profile to load the rating",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                    maxLines = 2,
                )
            } else {
                // Tier names like "international grandmaster" don't fit beside the rating on a
                // three-column widget, so they get their own line.
                val tier = rankTierFor(rating)
                val tierColor = ColorProvider(tier.color)
                Text(
                    text = "${snapshot.handle} $rating",
                    style = TextStyle(
                        color = tierColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = tier.name,
                    style = TextStyle(color = tierColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                )
            }
        }
    }
}

class GlanceWidgetUpdater(private val context: Context) : WidgetUpdater {
    override suspend fun update() {
        CFClimbWidget().updateAll(context)
    }
}

@Composable
actual fun rememberAddWidgetAction(): (() -> Unit)? {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val supported = remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }
    if (!supported) return null
    return remember(context, scope) {
        {
            scope.launch {
                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(CFClimbWidgetReceiver::class.java)
            }
        }
    }
}
