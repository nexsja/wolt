package lv.alexn.wolt.application

import lv.alexn.wolt.application.TimeType.Close
import lv.alexn.wolt.application.TimeType.Open
import lv.alexn.wolt.application.exception.InvalidScheduleStructure
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ampmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("a")
private val minuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(":mm")
private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h")

interface ScheduleFormatter {

    fun formatSchedule(rawSchedule: Schedule): String
}

@Service
class HumanReadableFormatter : ScheduleFormatter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun formatSchedule(rawSchedule: Schedule): String {

        val normalized = normalize(rawSchedule)
        val arrayOfWeekdays = Weekday.values()

        return arrayOfWeekdays.joinToString("\n") { workday ->
            val times = normalized[workday] ?: emptyList()
            if (times.isEmpty()) {
                "${workday.formatted}: Closed"
            } else {
                "${workday.formatted}: ${times.format()}"
            }
        }
    }

    private fun normalize(rawSchedule: Schedule): Map<Weekday, List<FlattenedOpeningHours>> {

        val flattened: MutableList<FlattenedSchedule> = mutableListOf()
        val arrayOfWeekdays = Weekday.values()

        // First we flatten the initial input to make more usable
        arrayOfWeekdays.forEach { weekday ->
            val hours = rawSchedule.getDay(weekday)
            hours.mapTo(flattened) {
                FlattenedSchedule(weekday, it)
            }
        }

        if (flattened.isEmpty()) {
            logger.info("Unable to format schedule - schedule is empty")
            return emptyMap()
        }

        // This fixes the list when the restaurant closes early monday morning.
        if (flattened.last().rawSchedule.type == Open && flattened.first().rawSchedule.type == Close) {
            flattened.add(flattened.removeFirst())
        }

        return flattened.chunked(2).map { chunk ->
            val first = chunk.first()
            val last = chunk.last()

            val day = first.day
            val open = first.rawSchedule.value
            val close = last.rawSchedule.value

            validateChunk(chunk, rawSchedule)

            FlattenedOpeningHours(day, open, close)
        }.groupBy(FlattenedOpeningHours::day)
    }

    // Detekt throws an error, while Kotlin recommends having these parentheses
    @Suppress("UnnecessaryParentheses")
    private fun validateChunk(chunk: List<FlattenedSchedule>, rawSchedule: Schedule) {
        val first = chunk.first()
        val last = chunk.last()

        val errorMessage = when (true) {
            (last.day.ordinal - first.day.ordinal > 1) ->
                "Invalid schedule structure -- opening times spanning multiple days are not supported."
            (first.rawSchedule.type != Open) ->
                "Invalid schedule structure -- first element should be 'Open', 'Close' encountered."
            (last.rawSchedule.type != Close) ->
                "Invalid schedule structure -- last element should be 'Close', 'Open' encountered."
            else -> ""
        }

        if (errorMessage.isNotEmpty()) {
            throw InvalidScheduleStructure(errorMessage, rawSchedule)
        }
    }

    private fun List<FlattenedOpeningHours>.format() =
        joinToString(", ") {
            "${it.open.formatTime()} - ${it.close.formatTime()}"
        }.uppercase()

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
