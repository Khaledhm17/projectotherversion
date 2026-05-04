package com.example.projectotherversion.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

val professionsList = listOf(
    "بناء", "كهربائي", "سباك", "نجار", "دهان", "ميكانيكي",
    "حداد", "ديكور", "ألمنيوم", "تكييف وتبريد", "أخرى"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionSpinner(
    selectedProfession: String,
    onProfessionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedProfession,
            onValueChange = {},
            readOnly = true,
            label = { Text("الحرفة") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            professionsList.forEach { profession ->
                DropdownMenuItem(
                    text = { Text(profession) },
                    onClick = {
                        onProfessionSelected(profession)
                        expanded = false
                    }
                )
            }
        }
    }
}