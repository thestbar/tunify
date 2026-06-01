package dev.thestbar.tunify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.thestbar.tunify.data.preferences.ThemePreference
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    themePreference: ThemePreference,
    isTunerLocked: Boolean,
    isLoadLastMutedState: Boolean,
    onThemeChange: (ThemePreference) -> Unit,
    onTunerLockedChange: (Boolean) -> Unit,
    onLoadLastMutedStateChange: (Boolean) -> Unit,
    onResetDatabase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Filled.RestartAlt, contentDescription = null) },
            title = { Text("Reset tunings?") },
            text = { Text("This removes your custom tunings and restores the default set. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onResetDatabase()
                    showResetDialog = false
                    snackMessage = "Tunings database reset"
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(snackMessage) {
        if (snackMessage != null) {
            delay(3200)
            snackMessage = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TunifyTopBar(title = "Settings")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingRow(
                    title = "Lock Tuner",
                    supporting = "Keep the current string selected",
                    trailing = { Switch(checked = isTunerLocked, onCheckedChange = onTunerLockedChange) },
                    onClick = { onTunerLockedChange(!isTunerLocked) }
                )
                SettingRow(
                    title = "Load Last Muted State",
                    supporting = "Restore mute on launch",
                    trailing = { Switch(checked = isLoadLastMutedState, onCheckedChange = onLoadLastMutedStateChange) },
                    onClick = { onLoadLastMutedStateChange(!isLoadLastMutedState) }
                )
                DesignDivider()
                SectionLabel("Appearance")
                SettingRow(
                    title = "Theme",
                    trailing = {
                        val options = listOf(ThemePreference.LIGHT to "Light", ThemePreference.DARK to "Dark")
                        SingleChoiceSegmentedButtonRow {
                            options.forEachIndexed { index, (pref, label) ->
                                SegmentedButton(
                                    selected = themePreference == pref,
                                    onClick = { onThemeChange(pref) },
                                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                )
                DesignDivider()
                SettingRow(
                    title = "Reset Tunings Database",
                    supporting = "Restore the built-in tuning list",
                    danger = true,
                    onClick = { showResetDialog = true }
                )
            }
        }

        snackMessage?.let { msg ->
            TunifySnackbar(
                message = msg,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
internal fun TunifySnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
