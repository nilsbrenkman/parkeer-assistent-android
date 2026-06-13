package nl.parkeerassistent.amsterdam.util

import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.data.model.RegimeDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Date/number helpers ported from iOS `util/Util`. java.time is available natively at minSdk 28.
 * Phase 5 adds the display formatters; this is the subset the Phase 4 ViewModels need.
 */
object DateUtil {

    /** App-facing wire format for *output* (`toWire`), matches iOS `dateTimeFormatter`. */
    private val wireFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

    /**
     * Lenient *parser* for wire date-times. The server's live path
     * (`DateUtil.dateFormatter`, pinned to Europe/Amsterdam, pattern `X`) emits a whole-hour
     * offset like `+02`, while mock/UTC paths emit `Z` and some callers emit `+02:00`. A strict
     * `XXX` formatter rejects `+02`, so live parking times failed to parse; the optional sections
     * accept all three forms.
     */
    private val wireParser: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][XX][X]")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val parkingDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    fun toWire(dateTime: OffsetDateTime): String = dateTime.format(wireFormatter)

    /** Parses a wire date-time, or null if malformed. */
    fun parseWire(value: String): OffsetDateTime? =
        runCatching { OffsetDateTime.parse(value, wireParser) }.getOrNull()

    /** Formats a wire date-time as `dd/MM HH:mm` for parking rows (iOS `parkingFormatter`). */
    fun formatParking(value: String): String =
        parseWire(value)?.format(parkingDisplayFormatter) ?: ""

    private val dayMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
    private val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonthFormatter)

    fun formatMonthYear(date: LocalDate): String = date.format(monthYearFormatter)

    /** Local date of a wire date-time (for history grouping / calendar chips). */
    fun toLocalDate(value: String): LocalDate? = parseWire(value)?.toLocalDate()

    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)

    /** Parking cost for [minutes] at [hourRate] €/h, formatted `0.00` (iOS `calculateCost`). */
    fun calculateCost(minutes: Int, hourRate: Double?): String {
        val rate = hourRate ?: return "0.00"
        return "%.2f".format(rate * minutes / 60.0)
    }

    /** Minutes of parking the balance buys at [hourRate] euro/hour (port of `calculateTimeBalance`). */
    fun calculateTimeBalance(balance: String?, hourRate: Double?): Int {
        val euros = balance?.toDoubleOrNull() ?: return 0
        if (hourRate == null || hourRate == 0.0) return 0
        return (euros / hourRate * 60).toInt()
    }

    /** The regime entry for [date]'s weekday, or null if that day has no paid regime. */
    fun getRegimeDay(regime: Regime, date: LocalDate): RegimeDay? {
        val key = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
            DayOfWeek.SATURDAY -> "SAT"
            DayOfWeek.SUNDAY -> "SUN"
        }
        return regime.days.firstOrNull { it.weekday == key }
    }

    /** Parses a `HH:mm` regime time, or null if malformed. */
    fun parseTime(time: String): LocalTime? =
        runCatching { LocalTime.parse(time, timeFormatter) }.getOrNull()
}
