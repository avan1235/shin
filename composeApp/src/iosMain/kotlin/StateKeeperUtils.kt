import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.json.Json
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

private val json: Json = Json {
    allowStructuredMapKeys = true
}

private const val STATE_KEY = "shin-state-key"

@Suppress("unused")
fun save(coder: NSCoder, state: SerializableContainer) {
    coder.encodeObject(`object` = json.encodeToString(SerializableContainer.serializer(), state), forKey = STATE_KEY)
}

@Suppress("unused")
fun restore(coder: NSCoder): SerializableContainer? {
    val string = coder.decodeTopLevelObjectOfClass(aClass = NSString, forKey = STATE_KEY, error = null) as? String?
        ?: return null
    return try {
        json.decodeFromString(SerializableContainer.serializer(), string)
    } catch (e: Exception) {
        null
    }
}
