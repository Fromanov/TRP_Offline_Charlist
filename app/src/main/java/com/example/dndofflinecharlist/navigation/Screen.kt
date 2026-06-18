package com.example.dndofflinecharlist.navigation

object Screen {
    const val LIST = "character_list"
    const val EDITOR = "character_editor/{characterId}"

    // Функция для безопасного формирования маршрута с аргументом
    fun editor(characterId: String): String = "character_editor/$characterId"
}