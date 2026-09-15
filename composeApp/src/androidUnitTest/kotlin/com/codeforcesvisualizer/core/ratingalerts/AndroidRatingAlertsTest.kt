package com.codeforcesvisualizer.core.ratingalerts

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.shared.domain.usecase.ObserveUserProfileUseCase
import com.codeforcesvisualizer.shared.domain.usecase.RefreshUserProfileUseCase
import com.codeforcesvisualizer.testing.FakeCFRepository
import com.codeforcesvisualizer.testing.InMemoryPreferencesDataStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

// SDK 34 keeps Robolectric on Java 17+; SDK 35 and up need Java 21.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AndroidRatingAlertsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val workManager get() = WorkManager.getInstance(context)
    private val scheduledChecks get() = workManager.getWorkInfosForUniqueWork(RATING_CHECK_WORK).get()

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun turningOnSchedulesAPeriodicCheckThatNeedsNetwork() {
        AndroidRatingCheckScheduler(context).setEnabled(true)

        val check = scheduledChecks.single()
        assertEquals(WorkInfo.State.ENQUEUED, check.state)
        assertEquals(NetworkType.CONNECTED, check.constraints.requiredNetworkType)
        assertEquals(TimeUnit.HOURS.toMillis(3), check.periodicityInfo?.repeatIntervalMillis)
    }

    @Test
    fun schedulingAgainKeepsTheExistingCheck() {
        val scheduler = AndroidRatingCheckScheduler(context)
        scheduler.setEnabled(true)
        val first = scheduledChecks.single().id

        scheduler.setEnabled(true)

        assertEquals(first, scheduledChecks.single().id)
    }

    @Test
    fun turningOffCancelsTheCheck() {
        val scheduler = AndroidRatingCheckScheduler(context)
        scheduler.setEnabled(true)

        scheduler.setEnabled(false)

        assertEquals(WorkInfo.State.CANCELLED, scheduledChecks.single().state)
    }

    @Test
    fun workerAsksForARetryWhenRatingsCannotBeLoaded() = runBlocking {
        val dataStore = InMemoryPreferencesDataStore()
        val repository = FakeCFRepository() // No responses, so every refresh fails.
        val alerts = RatingChangeAlerts(
            userSettings = UserSettingsRepository(dataStore).apply { setUsername("tourist") },
            settings = RatingAlertSettingsRepository(dataStore).apply { setEnabled(true) },
            refreshUserProfile = RefreshUserProfileUseCase(repository),
            observeUserProfile = ObserveUserProfileUseCase(repository),
            notifier = AndroidRatingChangeNotifier(context),
            scheduler = AndroidRatingCheckScheduler(context),
        )
        // The app's Application starts Koin; replace the real alerts with the test instance.
        loadKoinModules(module { single { alerts } })

        val result = TestListenableWorkerBuilder<RatingCheckWorker>(context).build().doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun alertIsPostedAsATaggedNotification() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val alert = RatingChangeAlert(
            handle = "tourist",
            contestId = 2,
            contestName = "Codeforces Round 2",
            oldRating = 3750,
            newRating = 3707,
            rank = 12,
        )

        AndroidRatingChangeNotifier(context).show(alert)

        val notifications = shadowOf(context.getSystemService(NotificationManager::class.java))
        val notification = notifications.getNotification(RATING_NOTIFICATION_TAG, 2)
        assertNotNull(notification)
        assertEquals("tourist: -43", shadowOf(notification).contentTitle)
        assertEquals("Codeforces Round 2: 3750 → 3707, rank 12", shadowOf(notification).contentText)
    }
}
