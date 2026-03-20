package com.theminimaldev.gymprint.widget

import android.content.Context
import android.graphics.*
import java.time.LocalDate

object WidgetBitmapRenderer {

    /**
     * Renders a mini contribution graph for the last [weeks] weeks.
     * Returns a Bitmap suitable for use as an ImageProvider in Glance.
     */
    fun render(
        context: Context,
        visitDates: Set<String>,
        weeks: Int = 12,
        cellSizePx: Int = 20,
        gapPx: Int = 4,
        filledColor: Int = Color.parseColor("#6750A4"),   // M3 purple seed — overridden at runtime
        emptyColor: Int = Color.parseColor("#E7E0EC")
    ): Bitmap {
        val cols = weeks
        val rows = 7
        val w = cols * (cellSizePx + gapPx) - gapPx
        val h = rows * (cellSizePx + gapPx) - gapPx

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val radius = cellSizePx / 4f

        val today = LocalDate.now()
        // Start from first day of the Sunday-aligned week, going back `weeks` weeks
        val startDate = today.minusWeeks(weeks.toLong() - 1)
            .with(java.time.DayOfWeek.MONDAY)

        for (col in 0 until cols) {
            for (row in 0 until rows) {
                val date = startDate.plusDays((col * 7 + row).toLong())
                if (date.isAfter(today)) continue

                val dateStr = date.toString()
                paint.color = if (visitDates.contains(dateStr)) filledColor else emptyColor

                val left = (col * (cellSizePx + gapPx)).toFloat()
                val top = (row * (cellSizePx + gapPx)).toFloat()
                canvas.drawRoundRect(
                    left, top, left + cellSizePx, top + cellSizePx,
                    radius, radius, paint
                )
            }
        }

        return bitmap
    }
}
