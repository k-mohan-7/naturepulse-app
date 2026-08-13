package com.simats.naturepulse.ui.nav

/** All navigation destinations in the app */
sealed class NavRoute(val route: String) {
    object Splash      : NavRoute("splash")
    object Login       : NavRoute("login")
    object Register    : NavRoute("register")
    object Dashboard   : NavRoute("dashboard")
    object Reports     : NavRoute("reports")
    object MyReports   : NavRoute("my_reports")
    object AddReport   : NavRoute("add_report")
    object Notifications : NavRoute("notifications")
    object Profile     : NavRoute("profile")
    object ReportDetail : NavRoute("report_detail/{reportId}") {
        fun withId(id: Int) = "report_detail/$id"
    }
}
