package lv.alexn.wolt.application

import java.time.DayOfWeek

enum class TimeType {
    OPEN,
    CLOSE
}

data class ScheduleEvent(val type: TimeType, val value: Int)
data class FlattenedScheduleDayEvent(val day: DayOfWeek, val event: ScheduleEvent)
data class FlattenedOpeningHours(val day: DayOfWeek, val open: Int, val close: Int)

typealias Schedule = Map<DayOfWeek, List<ScheduleEvent>>

fun Schedule.getEvents(day: DayOfWeek) = this[day] ?: emptyList()
