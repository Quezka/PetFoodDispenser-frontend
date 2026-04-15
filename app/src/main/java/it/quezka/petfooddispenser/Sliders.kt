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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

val sliderEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)

@Composable
fun LabelRow(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 10.dp,
                start = 10.dp,
                end = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        Text("1", style = MaterialTheme.typography.labelMedium)
        Text("2", style = MaterialTheme.typography.labelMedium)
        Text("3", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun DispenserSlider(
    label: String,
    value: Float,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    }

    Column(
        modifier = modifier.padding(bottom = 16.dp),
    ) {
        Text(text = "$label ${if (!enabled) stringResource(R.string.physical_suffix) else ""}")
        Slider(
            enabled = enabled,
            valueRange = 1f..3f,
            value = value,
            steps = 1,
            onValueChange = {
                if (it != value) {
                    vibrator.vibrate(sliderEffect)
                    onValueChange(it)
                }
            },
        )
        LabelRow()
        Text(text = stringResource(R.string.position_label, value.toInt()))
    }
}

@Composable
fun SliderCR1(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector_label, 1),
        value = value,
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun SliderCR2(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector_label, 2),
        value = value,
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun SliderCR3(value: Float, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    DispenserSlider(
        label = stringResource(R.string.selector_label, 3),
        value = value,
        enabled = enabled,
        onValueChange = onValueChange
    )
}
