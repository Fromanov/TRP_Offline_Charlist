package com.example.dndofflinecharlist.dnd5e

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dnd5eEditorScreen(viewModel: Dnd5eCharacterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (uiState as? Dnd5eUiState.Success)?.state?.name ?: "Редактор"
                    Text(name)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is Dnd5eUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is Dnd5eUiState.Error -> {
                    Text(
                        text = "Не удалось загрузить персонажа",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is Dnd5eUiState.Success -> {
                    val state = (uiState as Dnd5eUiState.Success).state
                    Dnd5eEditorForm(
                        state = state,
                        onNameChange = {}, // TODO: добавить редактирование имени
                        onStateChange = { statName, newValue ->
                            viewModel.updateStat(statName, newValue)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Dnd5eEditorForm(
    state: Dnd5eCharacterState,
    onNameChange: (String) -> Unit,
    onStateChange: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Основная информация", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Имя персонажа") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        OutlinedTextField(
            value = state.charClass,
            onValueChange = {}, // TODO: добавить обработку
            label = { Text("Класс") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        OutlinedTextField(
            value = state.level.toString(),
            onValueChange = {}, // TODO: добавить обработку
            label = { Text("Уровень") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Характеристики", style = MaterialTheme.typography.titleMedium)

        StatInputField(
            label = "Сила (Strength)",
            value = state.strength,
            onValueChange = { onStateChange("strength", it) }
        )

        StatInputField(
            label = "Ловкость (Dexterity)",
            value = state.dexterity,
            onValueChange = { onStateChange("dexterity", it) }
        )
    }
}

@Composable
private fun StatInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text ->
            if (text.all { it.isDigit() } && text.length <= 3) {
                val newValue = text.toIntOrNull() ?: 0
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}