package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(onDismiss: () -> Unit, onUpdate: () -> Unit, onIgnore: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Update",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "A new version of this app is now available!",
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onIgnore()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Red)
                    ) {
                        Text("Ignore")
                    }
                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                    ) {
                        Text("Later")
                    }
                    TextButton(
                        onClick = {
                            onUpdate()
                        },
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}