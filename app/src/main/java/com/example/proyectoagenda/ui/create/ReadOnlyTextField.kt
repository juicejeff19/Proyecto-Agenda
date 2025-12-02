package com.example.proyectoagenda.ui.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ReadOnlyTextField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        label = { Text(label) },
        readOnly = true,
        singleLine = true
    )
}
