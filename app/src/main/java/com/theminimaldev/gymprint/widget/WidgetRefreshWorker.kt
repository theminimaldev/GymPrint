package com.theminimaldev.gymprint.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        GymWidgetSmall().updateAll(applicationContext)
        GymWidgetMedium().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_TAG_PERIODIC = "widget_refresh_periodic"
        private const val WORK_TAG_ONCE = "widget_refresh_once"

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(1, TimeUnit.DAYS)
                .addTag(WORK_TAG_PERIODIC)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_TAG_PERIODIC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun enqueueOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .addTag(WORK_TAG_ONCE)
                .build()
            WorkManager.getInstance(context)
                .enqueue(request)
        }
    }
}
