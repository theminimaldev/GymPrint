package com.theminimaldev.gymprint.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.theminimaldev.gymprint.data.db.AppDatabase
import com.theminimaldev.gymprint.ui.theme.DarkColorScheme
import com.theminimaldev.gymprint.ui.theme.LightColorScheme
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ────────────────────────────────────────────────────────────────
// Small 2×2 Widget
// ────────────────────────────────────────────────────────────────

class GymWidgetSmall : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                SmallWidgetContent(streak = data.streak, lastVisitDate = data.lastVisitDate)
            }
        }
    }
}

class GymWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GymWidgetSmall()
}

// ────────────────────────────────────────────────────────────────
// Medium 4×2 Widget
// ────────────────────────────────────────────────────────────────

class GymWidgetMedium : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                MediumWidgetContent(streak = data.streak, visitDates = data.visitDates, context = context)
            }
        }
    }
}

class GymWidgetMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GymWidgetMedium()
}

// ────────────────────────────────────────────────────────────────
// Data helpers
// ────────────────────────────────────────────────────────────────

private data class WidgetData(
    val streak: Int,
    val lastVisitDate: String,
    val visitDates: Set<String>
)

private suspend fun loadWidgetData(context: Context): WidgetData {
    val db = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, "gymprint.db")
        .fallbackToDestructiveMigration().build()
    val visits = db.gymVisitDao().observeAll().first()
    db.close()

    val dateSet = visits.map { it.date }.toSortedSet()
    val streak = computeStreak(dateSet)
    val lastVisit = dateSet.lastOrNull() ?: ""
    val formatted = if (lastVisit.isNotEmpty()) {
        LocalDate.parse(lastVisit).format(DateTimeFormatter.ofPattern("d MMM"))
    } else "—"
    return WidgetData(streak = streak, lastVisitDate = formatted, visitDates = dateSet)
}

private fun computeStreak(sortedDates: Set<String>): Int {
    if (sortedDates.isEmpty()) return 0
    val today = LocalDate.now()
    var streak = 0
    var current = today
    while (sortedDates.contains(current.toString())) { streak++; current = current.minusDays(1) }
    if (streak == 0) {
        current = today.minusDays(1)
        while (sortedDates.contains(current.toString())) { streak++; current = current.minusDays(1) }
    }
    return streak
}

// ────────────────────────────────────────────────────────────────
// Glance composables
// ────────────────────────────────────────────────────────────────

@Composable
private fun SmallWidgetContent(streak: Int, lastVisitDate: String) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$streak",
                style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "day streak",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = if (lastVisitDate == "—") "No visits yet" else "Last: $lastVisitDate",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
            )
        }
    }
}

@Composable
private fun MediumWidgetContent(streak: Int, visitDates: Set<String>, context: Context) {
    val bitmap = WidgetBitmapRenderer.render(context = context, visitDates = visitDates, weeks = 12)
    Row(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.width(72.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$streak",
                style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "day streak",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 11.sp)
            )
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Contribution graph",
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}
