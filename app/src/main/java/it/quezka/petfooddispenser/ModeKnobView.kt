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

/**
 * A custom "Amplifier-style" knob for selecting dispenser modes.
 */
class ModeKnobView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle = -90f // Start pointing up
    
    // Discrete modes like an amplifier
    private val modes = listOf("OFF", "LOW", "MED", "HIGH")
    private var currentModeIndex = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val cx = width / 2f
        val cy = height / 2f
        val radius = Math.min(cx, cy) * 0.8f

        // 1. Draw the Outer Ring (The "Chassis")
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(cx, cy, radius + 10, paint)

        // 2. Draw the Knob (The "Dial")
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius, paint)

        // 3. Draw the Indicator Line (The "Marker")
        paint.color = Color.RED
        paint.strokeWidth = 15f
        val indicatorX = cx + (radius * 0.8f * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val indicatorY = cy + (radius * 0.8f * sin(Math.toRadians(angle.toDouble()))).toFloat()
        canvas.drawLine(cx, cy, indicatorX, indicatorY, paint)
        
        // 4. Draw Mode Labels
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("MODE: ${modes[currentModeIndex]}", cx, cy + radius + 80f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            val dx = event.x - (width / 2f)
            val dy = event.y - (height / 2f)
            
            // Math time! Use atan2 to get the angle of the touch (C++/Python style)
            val touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            
            // Snap to 4 positions (0, 90, 180, 270 degrees equivalent)
            // Simplified logic for this example:
            angle = touchAngle
            
            // Update mode based on angle
            updateMode(touchAngle)
            
            invalidate() // Force a redraw (like "repaint" in Java AWT/Swing)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun updateMode(angle: Float) {
        // Logic to switch modes based on where you point the knob
        currentModeIndex = when {
            angle in -135f..-45f -> 0 // TOP
            angle in -45f..45f -> 1   // RIGHT
            angle in 45f..135f -> 2    // BOTTOM
            else -> 3                  // LEFT
        }
    }
}
