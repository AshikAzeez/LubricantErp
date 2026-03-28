package com.havos.lubricerp.core.network

enum class AppEnvironment {
    TEST,
    STAGE,
    PRODUCTION;

    companion object {
        fun from(value: String): AppEnvironment {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: TEST
        }
    }
}
