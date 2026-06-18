package com.example.dndofflinecharlist.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import model.CharacterFile
import serialization.JsonConfig
import java.io.File

interface CharacterRepository {
    suspend fun getCharacter(id: String): CharacterFile?
    suspend fun saveCharacter(character: CharacterFile)
    suspend fun getAllCharacters(): List<CharacterFile>
    suspend fun deleteCharacter(id: String)
}

// 2. Реализация для файловой системы
class FileCharacterRepository(
    private val context: Context
) : CharacterRepository
{
    // Используем наш настроенный Json
    private val json = JsonConfig.default

    // Директория для хранения файлов внутри внутреннего хранилища приложения.
    // Эти файлы будут удалены только вместе с приложением.
    private val charactersDir: File
        get() = File(context.filesDir, "characters").apply {
            if (!exists()) mkdirs()
        }

    // Вспомогательный метод для получения файла по ID
    private fun getCharacterFile(id: String): File = File(charactersDir, "$id.json")

    override suspend fun getCharacter(id: String): CharacterFile? = withContext(Dispatchers.IO) {
        val file = getCharacterFile(id)
        if (file.exists()) {
            // Читаем текст и десериализуем в объект
            json.decodeFromString<CharacterFile>(file.readText())
        } else {
            null
        }
    }

    override suspend fun saveCharacter(character: CharacterFile) = withContext(Dispatchers.IO) {
        val targetFile = getCharacterFile(character.meta.id)

        // АТОМАРНОЕ СОХРАНЕНИЕ (Защита от сбоев)
        // 1. Создаем временный файл в той же директории
        val tempFile = File(charactersDir, "${character.meta.id}_temp.json")

        // 2. Записываем данные во временный файл
        tempFile.writeText(json.encodeToString(character))

        // 3. Переименовываем временный файл в целевой.
        // В файловых системах Linux/Android операция rename в пределах одной директории атомарна.
        // Если приложение упадет во время записи (шаг 2), старый файл останется целым.
        tempFile.renameTo(targetFile)
    }

    override suspend fun getAllCharacters(): List<CharacterFile> = withContext(Dispatchers.IO) {
        // Получаем все файлы с расширением .json, исключая временные файлы
        charactersDir.listFiles { file ->
            file.isFile && file.name.endsWith(".json") && !file.name.endsWith("_temp.json")
        }?.mapNotNull { file ->
            try {
                json.decodeFromString<CharacterFile>(file.readText())
            } catch (e: Exception) {
                // Если файл поврежден, просто игнорируем его, чтобы не крашить список
                null
            }
        } ?: emptyList()
    }

    override suspend fun deleteCharacter(id: String) = withContext(Dispatchers.IO) {
        getCharacterFile(id).delete()
    }
}