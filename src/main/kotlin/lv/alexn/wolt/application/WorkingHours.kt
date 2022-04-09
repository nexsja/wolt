package lv.alexn.wolt.application

enum class Weekday(val formatted: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");
}

enum class TimeType {
    Open,
    Close
}

data class OperatingHourEvent(val type: TimeType, val value: Int)
data class FlattenedSchedule(val day: Weekday, val rawSchedule: OperatingHourEvent)
data class FlattenedOpeningHours(val day: Weekday, val open: Int, val close: Int)

data class Schedule(
    val monday: List<OperatingHourEvent> = emptyList(),
    val tuesday: List<OperatingHourEvent> = emptyList(),
    val wednesday: List<OperatingHourEvent> = emptyList(),
    val thursday: List<OperatingHourEvent> = emptyList(),
    val friday: List<OperatingHourEvent> = emptyList(),
    val saturday: List<OperatingHourEvent> = emptyList(),
    val sunday: List<OperatingHourEvent> = emptyList()
) {

    fun getDay(day: Weekday) = when (day) {
        Weekday.MONDAY -> monday
        Weekday.TUESDAY -> tuesday
        Weekday.WEDNESDAY -> wednesday
        Weekday.THURSDAY -> thursday
        Weekday.FRIDAY -> friday
        Weekday.SATURDAY -> saturday
        Weekday.SUNDAY -> sunday
    }
}
