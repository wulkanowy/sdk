package io.github.wulkanowy.sdk.scrapper

import java.time.LocalDate
import java.time.LocalDateTime

open class BaseTest {

    fun getLocalDate(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month, day)
    }

    fun getLocalDateTime(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): LocalDateTime {
        return LocalDateTime.of(year, month, day, hour, minute, second)
    }

    fun getDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): LocalDateTime {
        return LocalDateTime.of(year, month, day, hour, minute, second)
    }
}
