package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CUSTOM_DHIKR_COLORS
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CreamText
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveLift

@Composable
fun AboutDedicationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OliveCard,
        titleContentColor = GoldGlow,
        textContentColor = CreamText,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(OliveLift)
                    .border(1.5.dp, GoldMain.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = GoldGlow,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 22.sp,
                    color = GoldGlow
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.about_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CreamText.copy(alpha = 0.92f),
                        lineHeight = 23.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(OliveLift)
                        .border(1.dp, GoldMain.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.about_dua),
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GoldGlow,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            GoldActionButton(
                text = stringResource(R.string.action_close),
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_close_button")
            )
        }
    )
}

@Composable
fun AddCustomDhikrDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, target: Int, colorHex: String, countsAsIstighfar: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("33") }
    var selectedColor by remember { mutableStateOf(CUSTOM_DHIKR_COLORS.first()) }
    var countsAsIstighfar by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldMain,
        unfocusedBorderColor = GoldMain.copy(alpha = 0.35f),
        focusedTextColor = CreamText,
        unfocusedTextColor = CreamText,
        focusedLabelColor = GoldGlow,
        unfocusedLabelColor = MutedGold,
        cursorColor = GoldGlow
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OliveCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.dialog_add_dhikr_title),
                style = MaterialTheme.typography.titleLarge.copy(color = GoldGlow)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text(stringResource(R.string.dialog_dhikr_text_label)) },
                    placeholder = {
                        Text(
                            stringResource(R.string.dialog_dhikr_text_hint),
                            color = MutedGold.copy(alpha = 0.65f)
                        )
                    },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_dhikr_title_input")
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(5)
                        targetText = digits
                    },
                    label = { Text(stringResource(R.string.dialog_dhikr_target_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_dhikr_target_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CUSTOM_DHIKR_COLORS.forEach { hex ->
                        val color = parseHexColor(hex)
                        val selected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) CreamText else OliveBg,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = OliveBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.dialog_dhikr_istighfar_toggle),
                        style = MaterialTheme.typography.bodyMedium.copy(color = CreamText),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = countsAsIstighfar,
                        onCheckedChange = { countsAsIstighfar = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OliveBg,
                            checkedTrackColor = GoldMain,
                            uncheckedThumbColor = MutedGold,
                            uncheckedTrackColor = OliveLift
                        )
                    )
                }
            }
        },
        confirmButton = {
            GoldActionButton(
                text = stringResource(R.string.action_save),
                enabled = title.trim().isNotEmpty(),
                onClick = {
                    val parsedTarget = targetText.toIntOrNull()?.coerceIn(0, 100_000) ?: 33
                    onAdd(title.trim(), parsedTarget, selectedColor, countsAsIstighfar)
                },
                modifier = Modifier.testTag("save_custom_dhikr_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MutedGold
                )
            }
        }
    )
}

@Composable
fun UserProfileDialog(
    currentDisplayName: String,
    allTimeTotal: Long,
    todayTotal: Long,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onSignOut: () -> Unit
) {
    var nameInput by remember(currentDisplayName) { mutableStateOf(currentDisplayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OliveCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.titleLarge.copy(color = GoldGlow)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(OliveLift)
                        .border(1.dp, GoldMain.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.metric_today_tasbih),
                            style = MaterialTheme.typography.labelSmall.copy(color = MutedGold)
                        )
                        Text(
                            text = formatCount(todayTotal),
                            style = MaterialTheme.typography.titleLarge.copy(color = GoldGlow)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.metric_all_time),
                            style = MaterialTheme.typography.labelSmall.copy(color = MutedGold)
                        )
                        Text(
                            text = formatCount(allTimeTotal),
                            style = MaterialTheme.typography.titleLarge.copy(color = CreamText)
                        )
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 40) nameInput = it },
                    label = { Text(stringResource(R.string.profile_display_name_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMain,
                        unfocusedBorderColor = GoldMain.copy(alpha = 0.35f),
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedLabelColor = GoldGlow,
                        unfocusedLabelColor = MutedGold,
                        cursorColor = GoldGlow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_display_name_input")
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ErrorCrimson.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ErrorCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.profile_sign_out),
                        color = ErrorCrimson,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            GoldActionButton(
                text = stringResource(R.string.action_save),
                enabled = nameInput.trim().isNotEmpty(),
                onClick = {
                    onSaveName(nameInput.trim())
                    onDismiss()
                },
                modifier = Modifier.testTag("profile_save_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MutedGold
                )
            }
        }
    )
}
