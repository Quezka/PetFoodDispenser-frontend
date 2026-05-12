package it.quezka.petfooddispenser

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun ModeSelector (
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    val options = listOf(
        stringResource(R.string.manual),
        stringResource(R.string.remote)
    )

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onSelectionChange(index) },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}