package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Localização necessária"
            )
        },

        text = {
            Text(
                text =
                    "O TVDE Tracker precisa da localização " +
                            "para registrar sua jornada mesmo com o " +
                            "ecrã bloqueado."
            )
        },

        confirmButton = {

            TextButton(
                onClick = onOpenSettings
            ) {
                Text(
                    text = "CONFIGURAÇÕES"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "CANCELAR"
                )
            }
        }
    )
}