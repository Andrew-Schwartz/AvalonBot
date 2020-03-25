package lib.model

import main.util.get

inline class Color(val value: UInt) {
    constructor(r: UInt, g: UInt, b: UInt) : this(
            r * 256u * 256u + g * 256u + b
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.color(): Color {
    val trimmed = if (startsWith("#") && length == 7) {
        removePrefix("#")
    } else if (startsWith("0x") && length == 8) {
        removePrefix("0x")
    } else {
        throw UnsupportedOperationException("color formatted like $this cannot be parsed")
    }
    val r = trimmed[0..1].toUInt(16)
    val g = trimmed[2..3].toUInt(16)
    val b = trimmed[4..5].toUInt(16)
    return Color(r, g, b)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Int.color(): Color = Color(toUInt())

@Suppress("NOTHING_TO_INLINE")
inline fun UInt.color(): Color = Color(this)