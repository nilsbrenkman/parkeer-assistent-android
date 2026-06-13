package nl.parkeerassistent.amsterdam.util

import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.data.model.RegimeDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DateUtilTest {

    @Test fun `cost is rounded to two decimals`() {
        assertEquals("2.40", DateUtil.calculateCost(60, 2.40))
        assertEquals("0.51", DateUtil.calculateCost(13, 2.37))
    }

    @Test fun `cost is zero without an hour rate`() =
        assertEquals("0.00", DateUtil.calculateCost(30, null))

    @Test fun `time balance is minutes the balance buys`() =
        assertEquals(500, DateUtil.calculateTimeBalance("20.00", 2.40))

    @Test fun `time balance is zero with missing inputs`() {
        assertEquals(0, DateUtil.calculateTimeBalance(null, 2.40))
        assertEquals(0, DateUtil.calculateTimeBalance("10", null))
    }

    @Test fun `regime day maps weekday to its entry`() {
        val regime = Regime(
            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").map { RegimeDay(it, "09:00", "21:00") },
        )
        // 2024-01-01 was a Monday; 2024-01-06 Saturday; 2024-01-07 Sunday.
        assertEquals("MON", DateUtil.getRegimeDay(regime, LocalDate.of(2024, 1, 1))?.weekday)
        assertEquals("SAT", DateUtil.getRegimeDay(regime, LocalDate.of(2024, 1, 6))?.weekday)
        assertEquals("SUN", DateUtil.getRegimeDay(regime, LocalDate.of(2024, 1, 7))?.weekday)
    }

    @Test fun `regime day is null when the weekday has no entry`() {
        val regime = Regime(listOf(RegimeDay("MON", "09:00", "21:00")))
        assertNull(DateUtil.getRegimeDay(regime, LocalDate.of(2024, 1, 2))) // Tuesday
    }

    @Test fun `parking time is formatted in its own offset`() =
        assertEquals("29/05 14:43", DateUtil.formatParking("2026-05-29T14:43:00+02:00"))

    @Test fun `malformed wire date formats to empty`() =
        assertEquals("", DateUtil.formatParking("not-a-date"))

    @Test fun `parseWire accepts the three offset forms the server emits`() {
        // Whole-hour offset (live path, pattern X), Z (mock/UTC), and +HH:mm (some callers).
        assertEquals(ZoneOffset.ofHours(2), DateUtil.parseWire("2026-05-29T14:43:00+02")?.offset)
        assertEquals(ZoneOffset.UTC, DateUtil.parseWire("2026-05-29T14:43:00Z")?.offset)
        assertEquals(ZoneOffset.ofHours(2), DateUtil.parseWire("2026-05-29T14:43:00+02:00")?.offset)
    }

    @Test fun `parseWire returns null on malformed input`() =
        assertNull(DateUtil.parseWire("garbage"))

    @Test fun `toWire round-trips through parseWire`() {
        val dt = OffsetDateTime.of(2026, 5, 29, 14, 43, 0, 0, ZoneOffset.ofHours(2))
        assertEquals("2026-05-29T14:43:00+02:00", DateUtil.toWire(dt))
        assertEquals(dt, DateUtil.parseWire(DateUtil.toWire(dt)))
    }

    @Test fun `toLocalDate extracts the date in the wire offset`() =
        assertEquals(LocalDate.of(2026, 5, 29), DateUtil.toLocalDate("2026-05-29T14:43:00+02:00"))

    @Test fun `toLocalDate returns null on malformed input`() =
        assertNull(DateUtil.toLocalDate("nope"))

    @Test fun `parseTime reads HH mm, rejects malformed`() {
        assertEquals(9, DateUtil.parseTime("09:00")?.hour)
        assertEquals(30, DateUtil.parseTime("21:30")?.minute)
        assertNull(DateUtil.parseTime("9 o'clock"))
    }

    @Test fun `formatTime renders HH mm`() =
        assertEquals("09:05", DateUtil.formatTime(LocalDateTime.of(2026, 1, 1, 9, 5)))
}
