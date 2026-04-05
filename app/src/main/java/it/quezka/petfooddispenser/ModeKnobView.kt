package it.quezka.petfooddispenser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

class ModeKnobView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 3 discrete modes
    private val modes = listOf("1", "2", "3")
    private var currentModeIndex = 0

    // Exact angles for each mode (clockwise)
    private val modeAngles = listOf(
        180f, // 1
        235f, // 2
        290f  // 3
    )

    // Current angle shown on the knob
    private var angle = modeAngles[currentModeIndex]

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) * 0.8f

        // Outer ring
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(cx, cy, radius + 10, paint)

        // Knob body
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius, paint)

        // Indicator line
        paint.color = Color.RED
        paint.strokeWidth = 15f
        val rad = Math.toRadians(angle.toDouble())
        val ix = cx + (radius * 0.8f * cos(rad)).toFloat()
        val iy = cy + (radius * 0.8f * sin(rad)).toFloat()
        canvas.drawLine(cx, cy, ix, iy, paint)

        // --- RADIAL LABELS ---
        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER

        val labelRadius = radius + 60f  // distance from center

        for (i in modes.indices) {
            val a = Math.toRadians(modeAngles[i].toDouble())
            val lx = cx + (labelRadius * cos(a)).toFloat()
            val ly = cy + (labelRadius * sin(a)).toFloat() + 15f // vertical centering

            canvas.drawText(modes[i], lx, ly, paint)
        }

        // Mode label at bottom
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("MODE: ${modes[currentModeIndex]}", cx, cy + radius + 120f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Prevent parent scroll views from intercepting
        parent.requestDisallowInterceptTouchEvent(true)

        val cx = width / 2f
        val cy = height / 2f

        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {

            val dx = event.x - cx
            val dy = event.y - cy

            // Raw angle from touch
            var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            if (touchAngle < 0) touchAngle += 360f

            // Determine nearest mode
            val snappedIndex = findNearestMode(touchAngle)

            // Update mode + angle (SNAP — no free rotation)
            currentModeIndex = snappedIndex
            angle = modeAngles[snappedIndex]

            invalidate()
            return true
        }

        return true // Always consume touch events
    }

    private fun findNearestMode(touchAngle: Float): Int {
        return modeAngles.indices.minByOrNull { i ->
            abs(modeAngles[i] - touchAngle)
        } ?: 0
    }
}
