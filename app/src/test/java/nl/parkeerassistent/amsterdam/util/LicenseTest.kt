package nl.parkeerassistent.amsterdam.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LicenseTest {

    @Test fun `2-3-1 sidecode`() = assertEquals("12-ABC-3", License.format("12ABC3"))

    @Test fun `2-2-2 sidecode`() = assertEquals("AB-12-CD", License.format("AB12CD"))

    @Test fun `1-3-2 sidecode`() = assertEquals("1-ABC-23", License.format("1ABC23"))

    @Test fun `3-2-1 sidecode`() = assertEquals("123-AB-4", License.format("123AB4"))

    @Test fun `1-2-3 sidecode`() = assertEquals("1-AB-234", License.format("1AB234"))

    @Test fun `normalises case and strips separators`() =
        assertEquals("12-ABC-3", License.format("12-abc-3"))

    @Test fun `too short is returned bare`() = assertEquals("ABC", License.format("ABC"))

    @Test fun `unrecognised length is returned bare`() =
        assertEquals("1234567", License.format("1234567"))
}
