package `in`.procyk.shin.shared

import kotlin.jvm.JvmInline

sealed interface Option<out A> {
    data object None : Option<Nothing>

    @JvmInline
    value class Some<A>(val value: A) : Option<A>

    companion object {
        fun <A> fromNullable(a: A?): Option<A> = if (a != null) Some(a) else None
    }
}

fun <A> Option<A>.toNullable(): A? = when (this) {
    is Option.Some -> value
    else -> null
}