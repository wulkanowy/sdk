package io.github.wulkanowy.sdk.mapper

import io.github.wulkanowy.sdk.mobile.register.Student
import io.github.wulkanowy.sdk.pojo.ReportingUnit
import io.github.wulkanowy.sdk.scrapper.messages.ReportingUnit as ScrapperReportingUnit

fun List<ScrapperReportingUnit>.mapReportingUnits() = map {
    ReportingUnit(
        id = it.unitId,
        roles = it.roles,
        senderId = it.senderId,
        senderName = it.senderName,
        short = it.short
    )
}

fun List<Student>.mapReportingUnits(studentId: Int) = filter { studentId == it.loginId }.map {
    ReportingUnit(
        id = it.reportingUnitId,
        short = it.reportingUnitShortcut,
        senderName = it.name,
        senderId = it.id,
        roles = emptyList()
    )
}
