package com.example.dndofflinecharlist.dnd5e

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndofflinecharlist.data.repository.CharacterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import model.CharacterFile
import model.Meta

// 1. Строгая модель данных конкретно для экрана D&D 5e.
// UI работает с ней, а не с "сырым" JSON. Это упрощает разработку интерфейса.
data class Dnd5eCharacterState(
    val id: String,
    val name: String,
    val charClass: String,
    val level: Int,
    val strength: Int,
    val dexterity: Int
    // В будущем сюда добавятся остальные характеристики и навыки
)

// 2. Состояние экрана (UI State).
// Помогает UI понять, что показывать: прогресс-бар, ошибку или сам лист.
sealed interface Dnd5eUiState {
    object Loading : Dnd5eUiState
    data class Success(val state: Dnd5eCharacterState) : Dnd5eUiState
    object Error : Dnd5eUiState
}

class Dnd5eCharacterViewModel(
    private val repository: CharacterRepository,
    private val characterId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<Dnd5eUiState>(Dnd5eUiState.Loading)
    val uiState: StateFlow<Dnd5eUiState> = _uiState.asStateFlow()

    // Переменная для хранения задачи автосохранения
    private var saveJob: Job? = null
    private val AUTO_SAVE_DELAY_MS = 1500L // Задержка перед сохранением (1.5 секунды)

    init {
        loadCharacter()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            try {
                val file = repository.getCharacter(characterId)
                if (file != null) {
                    _uiState.value = Dnd5eUiState.Success(mapToState(file))
                } else {
                    _uiState.value = Dnd5eUiState.Error
                }
            } catch (e: Exception) {
                // В реальном проекте здесь можно залогировать ошибку
                _uiState.value = Dnd5eUiState.Error
            }
        }
    }

    /**
     * Вызывается из UI при изменении любого поля.
     * @param statName имя поля (например, "strength")
     * @param newValue новое значение
     */
    fun updateStat(statName: String, newValue: Int) {
        // Получаем текущее состояние. Если оно не Success, ничего не делаем.
        val currentState = (_uiState.value as? Dnd5eUiState.Success)?.state ?: return

        // 1. Мгновенно обновляем состояние в памяти (чтобы UI сразу отрисовал новую цифру)
        val updatedState = when (statName) {
            "strength" -> currentState.copy(strength = newValue)
            "dexterity" -> currentState.copy(dexterity = newValue)
            else -> currentState
        }

        _uiState.value = Dnd5eUiState.Success(updatedState)

        // 2. Запускаем таймер автосохранения
        scheduleSave(updatedState)
    }

    // Механизм Debounce (Защита от частых записей на диск)
    private fun scheduleSave(state: Dnd5eCharacterState) {
        // Если пользователь продолжает печатать/менять значения,
        // мы отменяем предыдущий таймер, чтобы не писать файл 10 раз в секунду.
        saveJob?.cancel()

        saveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS) // Ждем 1.5 секунды тишины
            saveToDisk(state)         // И только потом пишем на диск
        }
    }

    private suspend fun saveToDisk(state: Dnd5eCharacterState) {
        // Конвертируем строгую D&D модель обратно в гибкий JsonObject
        val jsonObj = buildJsonObject {
            put("charClass", JsonPrimitive(state.charClass))
            put("strength", JsonPrimitive(state.strength))
            put("dexterity", JsonPrimitive(state.dexterity))
        }

        // Собираем итоговый файл
        val file = CharacterFile(
            meta = Meta(
                id = state.id,
                name = state.name,
                system = "dnd5e",
                level = state.level,
                modified = System.currentTimeMillis() // Обновляем время изменения
            ),
            jsonObj = jsonObj
        )

        // Сохраняем через репозиторий (он сам сделает это атомарно и в фоне)
        repository.saveCharacter(file)
    }

    // Маппинг: превращаем "сырой" JSON из файла в удобное состояние для UI
    private fun mapToState(file: CharacterFile): Dnd5eCharacterState {
        return Dnd5eCharacterState(
            id = file.meta.id,
            name = file.meta.name,
            charClass = file.jsonObj["charClass"]?.jsonPrimitive?.contentOrNull ?: "Воин",
            level = file.meta.level,
            strength = file.jsonObj["strength"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 10,
            dexterity = file.jsonObj["dexterity"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 10
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Гарантируем, что при закрытии экрана мы отменяем фоновые задачи
        saveJob?.cancel()
    }
}