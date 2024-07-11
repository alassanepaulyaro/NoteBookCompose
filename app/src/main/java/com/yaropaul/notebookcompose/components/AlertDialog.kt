package com.yaropaul.notebookcompose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

/**
 * Display Alert Dialog
 */
@Composable
fun DisplayAlertDialog(
    title: String,
    message: String,
    dialogOpened: Boolean,
    onDialogClose: () -> Unit,
    onYesClicked: () -> Unit
) {
    if (dialogOpened) {
        AlertDialog(
            title = {
                Text(
                    text = title,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Normal
                )
            },
            onDismissRequest = onDialogClose,
            confirmButton = {
                Button(onClick = {
                    onYesClicked()
                    onDialogClose()
                }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onDialogClose() }) {
                    Text(text = "No")
                }
            }
        )
    }
}


@Composable
@Preview
fun DisplayAlertDialogPreview() {
    DisplayAlertDialog("Alert Dialog", "Display Dialog", true, {}, {})
}