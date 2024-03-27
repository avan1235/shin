package `in`.procyk.shin.model

enum class ShortenedProtocol(val presentableName: String) {
    HTTPS("https"),
    HTTP("http"),
    ;

    fun buildUrl(url: String): String {
        val idx = url.indexOf(ProtocolSeparator)
        val noProtocolUrl = if (idx != -1) url.drop(idx + ProtocolSeparator.length) else url
        return presentableName + ProtocolSeparator + noProtocolUrl
    }

    companion object {
        fun simplifyInputUrl(url: String): Pair<String, ShortenedProtocol?> {
            for (protocol in ShortenedProtocol.entries) {
                val prefix = protocol.presentableName + ProtocolSeparator
                if (url.startsWith(prefix)) {
                    return Pair(url.drop(prefix.length), protocol)
                }
            }
            return Pair(url, null)
        }
    }
}

private const val ProtocolSeparator: String = "://"
