import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate(): LocalDate =
    toLocalDateTime(TimeZone.currentSystemDefault()).date
