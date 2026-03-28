package com.havos.lubricerp.core.common

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun from(value: String): ThemeMode {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: SYSTEM
        }
    }
}
