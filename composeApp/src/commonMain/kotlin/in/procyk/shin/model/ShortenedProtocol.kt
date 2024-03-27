package `in`.procyk.shin.model

enum class ShortenedProtocol(val presentableName: String) {
    HTTPS("https"),
    HTTP("http"),
    ;

    fun buildUrl(url: String): String {
        val idx = url.indexOf(PROTOCOL_SEPARATOR)
        val noProtocolUrl = if (idx != -1) url.drop(idx + PROTOCOL_SEPARATOR.length) else url
        return presentableName + PROTOCOL_SEPARATOR + noProtocolUrl
    }

    companion object {
        fun simplifyInputUrl(url: String): Pair<String, ShortenedProtocol?> {
            for (protocol in ShortenedProtocol.entries) {
                val prefix = protocol.presentableName + PROTOCOL_SEPARATOR
                if (url.startsWith(prefix)) {
                    return Pair(url.drop(prefix.length), protocol)
                }
            }
            return Pair(url, null)
        }
    }
}

private const val PROTOCOL_SEPARATOR: String = "://"
