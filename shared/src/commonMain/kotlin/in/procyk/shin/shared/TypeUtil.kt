package `in`.procyk.shin.shared

inline fun <T : Any> T.applyIf(condition: Boolean, action: T.() -> T): T = if (condition) action(this) else this
