import kotlinx.datetime.*

fun Instant.toLocalDate(): LocalDate =
    toLocalDateTime(TimeZone.currentSystemDefault()).date
