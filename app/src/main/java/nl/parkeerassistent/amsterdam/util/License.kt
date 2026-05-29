package nl.parkeerassistent.amsterdam.util

/**
 * Dutch license-plate grouping (port of iOS `util/License`). Normalises to A–Z/0–9 and inserts
 * dashes for the recognised 6-character sidecode patterns; otherwise returns the bare characters.
 */
object License {

    fun format(license: String): String {
        val chars = normalise(license)
        return formats.firstOrNull { it.matches(chars) }?.format?.invoke(chars) ?: chars.joinToString("")
    }

    private fun normalise(s: String): List<Char> =
        s.uppercase().filter { it in '0'..'9' || it in 'A'..'Z' }.toList()

    private fun isDigit(c: Char) = c in '0'..'9'

    private fun isSame(a: Char, b: Char) = isDigit(a) == isDigit(b)

    /** True if every char in the (inclusive) range is the same kind (all digits or all letters). */
    private fun List<Char>.isGroup(from: Int, to: Int): Boolean {
        val first = this[from]
        for (i in (from + 1)..to) if (!isSame(first, this[i])) return false
        return true
    }

    private fun List<Char>.dash(vararg groups: IntRange): String =
        groups.joinToString("-") { range -> range.joinToString("") { this[it].toString() } }

    private class Pattern(val matches: (List<Char>) -> Boolean, val format: (List<Char>) -> String)

    private val formats = listOf(
        // 2-2-2
        Pattern(
            matches = { it.size == 6 && it.isGroup(0, 1) && it.isGroup(2, 3) && it.isGroup(4, 5) },
            format = { it.dash(0..1, 2..3, 4..5) },
        ),
        // 2-3-1
        Pattern(
            matches = { it.size == 6 && it.isGroup(0, 1) && it.isGroup(2, 4) },
            format = { it.dash(0..1, 2..4, 5..5) },
        ),
        // 1-3-2
        Pattern(
            matches = { it.size == 6 && it.isGroup(1, 3) && it.isGroup(4, 5) },
            format = { it.dash(0..0, 1..3, 4..5) },
        ),
        // 3-2-1
        Pattern(
            matches = { it.size == 6 && it.isGroup(0, 2) && it.isGroup(3, 4) },
            format = { it.dash(0..2, 3..4, 5..5) },
        ),
        // 1-2-3
        Pattern(
            matches = { it.size == 6 && it.isGroup(1, 2) && it.isGroup(3, 5) },
            format = { it.dash(0..0, 1..2, 3..5) },
        ),
    )
}
