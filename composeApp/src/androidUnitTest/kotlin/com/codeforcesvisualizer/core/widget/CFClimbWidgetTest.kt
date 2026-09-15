package com.codeforcesvisualizer.core.widget

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasStartActivityClickAction
import androidx.glance.testing.unit.hasText
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codeforcesvisualizer.HomeActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val NOW = 1_789_500_000L

// SDK 34 keeps Robolectric on Java 17+; SDK 35 and up need Java 21.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CFClimbWidgetTest {

    private val snapshot = WidgetSnapshot(
        nextRound = NextRound(2266, "Codeforces Round (Div. 3)", NOW + 6 * 86_400 + 4 * 3_600 + 30),
        handle = "tourist",
        rating = 3301,
    )

    @Test
    fun mediumWidgetShowsTheNextRoundAndRating() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        setAppWidgetSize(CFClimbWidget.MediumSize)

        provideComposable { WidgetContent(snapshot, NOW) }

        onNode(hasTextEqualTo("Codeforces Round (Div. 3)")).assertExists()
        onNode(hasText("in 6d 4h")).assertExists()
        onNode(hasTextEqualTo("tourist 3301")).assertExists()
        onNode(hasTextEqualTo("legendary grandmaster")).assertExists()
        onNode(hasStartActivityClickAction<HomeActivity>()).assertExists()
    }

    @Test
    fun smallWidgetLeavesOutTheRating() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        setAppWidgetSize(CFClimbWidget.SmallSize)

        provideComposable { WidgetContent(snapshot, NOW) }

        onNode(hasTextEqualTo("Codeforces Round (Div. 3)")).assertExists()
        onNode(hasText("tourist")).assertDoesNotExist()
    }

    @Test
    fun emptyWidgetExplainsHowToFillIt() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        setAppWidgetSize(CFClimbWidget.MediumSize)

        provideComposable { WidgetContent(WidgetSnapshot(null, null, null), NOW) }

        onNode(hasTextEqualTo("No upcoming rounds saved. Open CFClimb to refresh.")).assertExists()
    }
}
