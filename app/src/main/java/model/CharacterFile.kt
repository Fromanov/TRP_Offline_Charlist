package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

//Основной контейнер листа.
@Serializable
data class CharacterFile (
    val meta: Meta,
    val jsonObj: JsonObject
)

//Метадата для удобство организации поиска
@Serializable
data class Meta (
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Безымянный герой",
    val system: String = "dnd5e",
    val edition: String? = null,
    val avatarBase64: String? = null,
    val level: Int = 1,
    val created: Long = System.currentTimeMillis(),
    val modified: Long = System.currentTimeMillis()
)