package lv.alexn.wolt.application

import lv.alexn.wolt.application.TimeType.CLOSE
import lv.alexn.wolt.application.TimeType.OPEN
import lv.alexn.wolt.application.exception.InvalidScheduleStructure
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Should probably be configurable
private val locale = Locale.US

private val hourFormatter = DateTimeFormatter.ofPattern("h", locale)
private val minuteFormatter = DateTimeFormatter.ofPattern(":mm", locale)
private val ampmFormatter = DateTimeFormatter.ofPattern("a", locale)

@Service
class HumanReadableFormatter {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun formatSchedule(rawSchedule: Schedule): String {

        val normalized = normalize(rawSchedule)

        return DayOfWeek.values().joinToString("\n") { workday ->
            val times = normalized[workday] ?: emptyList()
            val displayName = workday.getDisplayName(TextStyle.FULL, locale)
            if (times.isEmpty()) {
                "$displayName: Closed"
            } else {
                "$displayName: ${times.format()}"
            }
        }
    }

    private fun normalize(rawSchedule: Schedule): Map<DayOfWeek, List<FlattenedOpeningHours>> {

        // First we flatten the initial input to make more usable
        val events = DayOfWeek.values()
            .flatMap { weekday ->
                rawSchedule
                    .getEvents(weekday)
                    .map {
                        FlattenedScheduleDayEvent(weekday, it)
                    }
            }.toMutableList()

        if (events.isEmpty()) {
            logger.info("Schedule formatting request is empty")
            return emptyMap()
        }

        // This fixes the list when the restaurant closes early monday morning.
        val lastEvent = events.last()
        val firstEvent = events.first()
        if (
            lastEvent.day == SUNDAY && lastEvent.event.type == OPEN &&
            firstEvent.day == MONDAY && firstEvent.event.type == CLOSE
        ) {
            events.add(events.removeFirst())
        }

        return events
            .asSequence()
            .chunked(2)
            .map { validateChunk(it, rawSchedule) }
            .map { (open, close) ->
                FlattenedOpeningHours(
                    day = open.day,
                    open = open.event.value,
                    close = close.event.value
                )
            }
            .groupBy(FlattenedOpeningHours::day)
    }

    private fun validateChunk(
        pair: List<FlattenedScheduleDayEvent>,
        rawSchedule: Schedule
    ): Pair<FlattenedScheduleDayEvent, FlattenedScheduleDayEvent> {
        if (pair.size != 2) {
            throw InvalidScheduleStructure(
                "Invalid schedule structure -- day schedule chunk has invalid open/close pair.",
                rawSchedule
            )
        }

        val open = pair.first()
        val close = pair.last()

        val errorMessage = when {
            close.day.ordinal - open.day.ordinal > 1 ->
                "Invalid schedule structure -- opening times spanning multiple days are not supported."
            open.event.type != OPEN ->
                "Invalid schedule structure -- first element should be 'Open', 'Close' encountered."
            close.event.type != CLOSE ->
                "Invalid schedule structure -- last element should be 'Close', 'Open' encountered."
            else -> null
        }

        errorMessage?.let {
            throw InvalidScheduleStructure(it, rawSchedule)
        }

        return open to close
    }

    private fun List<FlattenedOpeningHours>.format() =
        joinToString(", ") {
            "${it.open.formatTime()} - ${it.close.formatTime()}"
        }

    private fun Int.formatTime(): String {
        val localTime = LocalTime.ofSecondOfDay(this.toLong())
        val hours = localTime.format(hourFormatter)
        val minutes = if (localTime.minute > 0) {
            localTime.format(minuteFormatter)
        } else {
            ""
        }

        // Apparently (naming|https://english.stackexchange.com/a/35317) this variable is... complicated?
        val ampm = localTime.format(ampmFormatter)
        return "$hours$minutes $ampm"
    }
}
