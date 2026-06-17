package serialization

import kotlinx.serialization.json.Json

object JsonConfig {
    val default: Json = Json{
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
}