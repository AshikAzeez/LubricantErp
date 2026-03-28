package com.havos.lubricerp.feature_reports.presentation.navigation

object AppRoutes {
    const val GATE = "gate"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val REPORT_DETAIL = "report_detail/{reportKey}"

    fun reportDetail(reportKey: String): String = "report_detail/$reportKey"
}
