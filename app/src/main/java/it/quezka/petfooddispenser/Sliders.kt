package it.quezka.petfooddispenser

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val sliderEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)

@Composable
fun LabelRow(labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        labels.forEach { label ->
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DispenserSlider(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    labels: List<String>,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        // We use a try-catch here because the Vibrator service isn't always available in Previews
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = modifier.padding(bottom = 16.dp),
    ) {
        Text(text = "$label ${if (!enabled) stringResource(R.string.physical_suffix) else ""}")
        Slider(
            enabled = enabled,
            // valueRange should match the number of labels
            valueRange = 1f..labels.size.toFloat(),
            value = value,
            // steps is (number of discrete values - 2)
            steps = labels.size - 2,
            onValueChange = {
                if (it != value) {
                    vibrator?.vibrate(sliderEffect)
                    onValueChange(it)
                }
            },
        )
        LabelRow(labels = labels)

        // Show the actual label name instead of just the number
        val displayValue = labels.getOrNull(value.toInt() - 1) ?: value.toInt().toString()
        Text(text = stringResource(R.string.position_label, displayValue))
    }
}

@Composable
fun SliderCR1(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector1_label),
        value = value,
        labels = listOf("1h", "2h", "3h", "4h", "5h"),
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun SliderCR2(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector2_label),
        value = value,
        labels = listOf("1h", "2h", "3h", "4h", "5h"),
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun SliderCR3(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector3_label),
        value = value,
        labels = listOf("XS", "S", "M", "L", "XL"),
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Preview(showBackground = true, name = "Pet Feeder Sliders")
@Composable
fun SlidersPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                // 'var ... by remember' creates local state for the preview
                var val1 by remember { mutableFloatStateOf(1f) }
                var val2 by remember { mutableFloatStateOf(3f) }
                var val3 by remember { mutableFloatStateOf(2f) }

                SliderCR1(value = val1, onValueChange = { val1 = it })
                SliderCR2(value = val2, onValueChange = { val2 = it })
                SliderCR3(value = val3, onValueChange = { val3 = it })
            }
        }
    }
}
