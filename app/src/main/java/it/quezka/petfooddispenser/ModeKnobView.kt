package it.quezka.petfooddispenser

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModeKnob(
    modifier: Modifier = Modifier,
    onModeChanged: (Int) -> Unit = {}
) {
    // State: Which mode is currently selected
    var currentModeIndex by remember { mutableIntStateOf(0) }

    val modes = listOf("1", "2", "3")
    val modeAngles = listOf(180f, 235f, 290f)
    val currentAngle = modeAngles[currentModeIndex]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        // Calculate angle from touch position
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        val touchX = change.position.x - cx
                        val touchY = change.position.y - cy

                        var touchAngle = Math.toDegrees(atan2(touchY.toDouble(), touchX.toDouble())).toFloat()
                        if (touchAngle < 0) touchAngle += 360f

                        // Determine nearest mode index
                        val nearest = modeAngles.indices.minBy { i ->
                            abs(modeAngles[i] - touchAngle)
                        }

                        if (nearest != currentModeIndex) {
                            currentModeIndex = nearest
                            onModeChanged(nearest)
                        }
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.4f

            // Outer ring
            drawCircle(
                color = Color.LightGray,
                center = center,
                radius = radius + 10f,
                style = Stroke(width = 10f)
            )

            // Knob body
            drawCircle(
                color = Color.DarkGray,
                center = center,
                radius = radius
            )

            // Indicator line
            val rad = Math.toRadians(currentAngle.toDouble())
            val ix = cx + (radius * 0.8f * cos(rad)).toFloat()
            val iy = cy + (radius * 0.8f * sin(rad)).toFloat()

            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(ix, iy),
                strokeWidth = 15f
            )
        }

        // Mode label at bottom
        Text(
            text = "MODE: ${modes[currentModeIndex]}",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
