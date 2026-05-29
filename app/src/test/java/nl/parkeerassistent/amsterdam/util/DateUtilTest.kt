package nl.parkeerassistent.amsterdam.util

import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.data.model.RegimeDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

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
}
