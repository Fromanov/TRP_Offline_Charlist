package data.repository

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

