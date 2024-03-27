import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

private val json: Json = Json {
    allowStructuredMapKeys = true
}

private const val StateKey = "shin-state-key"

@Suppress("unused")
fun save(coder: NSCoder, state: SerializableContainer) {
    coder.encodeObject(`object` = json.encodeToString(SerializableContainer.serializer(), state), forKey = StateKey)
}

@Suppress("unused")
fun restore(coder: NSCoder): SerializableContainer? {
    val string = coder.decodeTopLevelObjectOfClass(aClass = NSString, forKey = StateKey, error = null) as? String?
        ?: return null
    return try {
        json.decodeFromString(SerializableContainer.serializer(), string)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
