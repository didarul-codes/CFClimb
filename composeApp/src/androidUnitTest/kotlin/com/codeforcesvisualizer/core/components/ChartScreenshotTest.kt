package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codeforcesvisualizer.core.theme.CFTheme
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every chart with fixed data in the light theme (top) and dark theme (bottom).
 *
 * Record baselines: ./gradlew :composeApp:recordRoborazziDebug
 * Check for changes: ./gradlew :composeApp:verifyRoborazziDebug
 *
 * SDK 34 keeps the tests on Java 17+; Robolectric needs Java 21 for SDK 35 and up.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ChartScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Text antialiasing differs slightly between macOS and the Linux CI runner.
    private val options = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f)
    )

    @Test
    fun ratingLineChart() = snapshot("rating_line_chart") {
        val colors = CFThemeColors.current
        RatingLineChart(
            series = listOf(
                RatingSeriesData(
                    data = SampleRatings.mapIndexed { index, rating -> RatingPoint(index, rating) },
                    color = colors.violet,
                    label = "rating",
                ),
                RatingSeriesData(
                    data = SampleRatings.mapIndexed { index, rating -> RatingPoint(index, rating - 150 + index * 20) },
                    color = colors.green,
                    label = "rival",
                ),
            ),
        )
    }

    @Test
    fun difficultyHistogram() = snapshot("difficulty_histogram") {
        DifficultyHistogram(
            buckets = (800..3500 step 100).mapIndexed { index, rating ->
                DifficultyBucket(range = rating.toString(), count = (index * 37 % 23) + 1)
            },
        )
    }

    @Test
    fun tagBarsChart() = snapshot("tag_bars_chart") {
        TagBarsChart(
            tags = listOf(
                TagData("greedy", 948),
                TagData("math", 921),
                TagData("dp", 715),
                TagData("implementation", 677),
                TagData("constructive algorithms", 595),
                TagData("data structures", 557),
                TagData("brute force", 480),
                TagData("graphs", 362),
            ),
        )
    }

    @Test
    fun submissionHeatmap() = snapshot("submission_heatmap") {
        SubmissionHeatmap(
            cells = (0 until 26).flatMap { week ->
                (0..6).map { day -> HeatmapCell(week, day, count = (week * 3 + day * 5) % 7) }
            },
        )
    }

    @Test
    fun verdictDonut() = snapshot("verdict_donut") {
        VerdictDonut(
            data = mapOf("AC" to 620, "WA" to 210, "TLE" to 64, "RE" to 31, "CE" to 12, "MLE" to 5),
        )
    }

    @Test
    fun languageBarChart() = snapshot("language_bar_chart") {
        LanguageBarChart(
            languages = listOf(
                LanguageData("C++20 (GCC 13-64)", 62f),
                LanguageData("Python 3", 21f),
                LanguageData("Kotlin 1.9", 12f),
                LanguageData("Java 21", 5f),
            ),
        )
    }

    @Test
    fun tagRadarChart() = snapshot("tag_radar_chart") {
        val colors = CFThemeColors.current
        TagRadarChart(
            series = listOf(
                RadarSeries(values = listOf(90, 75, 60, 40, 55, 30, 45, 20), color = colors.violet),
                RadarSeries(values = listOf(60, 80, 70, 65, 35, 50, 25, 40), color = colors.green),
            ),
            axes = listOf("greedy", "math", "dp", "graphs", "strings", "trees", "geometry", "games"),
        )
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            Column {
                ThemedPanel(isDarkTheme = false, content = content)
                ThemedPanel(isDarkTheme = true, content = content)
            }
        }
        composeRule.onRoot().captureRoboImage(
            filePath = "src/androidUnitTest/screenshots/$name.png",
            roborazziOptions = options,
        )
    }
}

@Composable
private fun ThemedPanel(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    CFTheme(isDarkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CFThemeColors.current.bg)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

private val SampleRatings = listOf(1200, 1350, 1310, 1480, 1520, 1605, 1580, 1710, 1690, 1840, 1905, 1880)
