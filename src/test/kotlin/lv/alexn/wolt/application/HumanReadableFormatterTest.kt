package lv.alexn.wolt.application

import lv.alexn.wolt.application.TimeType.CLOSE
import lv.alexn.wolt.application.TimeType.OPEN
import lv.alexn.wolt.application.exception.InvalidScheduleStructure
import lv.alexn.wolt.extension.KArgumentsProvider
import lv.alexn.wolt.extension.NamedTestCase
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY

class HumanReadableFormatterTest {

    private val formatter = HumanReadableFormatter()

    @Nested
    inner class HappyPath {

        @ParameterizedTest
        @ArgumentsSource(FormatArgumentsProvider::class)
        fun `successfully format`(args: TestCase) {

            assertThat(formatter.formatSchedule(args.schedule)).isEqualTo(args.expected)
        }
    }

    @Nested
    inner class Exception {

        @ParameterizedTest
        @ArgumentsSource(ExceptionCaseProvider::class)
        fun `test exception being thrown on invalid structure`(args: TestCase) {

            assertThatThrownBy {
                formatter.formatSchedule(args.schedule)
            }
                .isInstanceOf(InvalidScheduleStructure::class.java)
                .hasMessage(args.expected)
        }
    }
}

data class TestCase(
    val schedule: Schedule,
    val expected: String,
    override val name: String? = null
) : NamedTestCase

private class ExceptionCaseProvider : KArgumentsProvider<TestCase>(
    TestCase(
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(CLOSE, 3900))
        ),
        expected = "Invalid schedule structure -- day schedule chunk has invalid open/close pair."
    ),
    TestCase(
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(CLOSE, 3900), ScheduleEvent(CLOSE, 7200))
        ),
        expected = "Invalid schedule structure -- first element should be 'Open', 'Close' encountered."
    ),
    TestCase(
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(OPEN, 3900), ScheduleEvent(OPEN, 7200))
        ),
        expected = "Invalid schedule structure -- last element should be 'Close', 'Open' encountered."
    ),
    TestCase(
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(OPEN, 3600)),
            FRIDAY to listOf(ScheduleEvent(CLOSE, 43200))
        ),
        expected = "Invalid schedule structure -- opening times spanning multiple days are not supported."
    ),
)

private class FormatArgumentsProvider : KArgumentsProvider<TestCase>(
    TestCase(
        name = "Test when schedule is empty",
        schedule = emptyMap(),
        expected = """
            Monday: Closed
            Tuesday: Closed
            Wednesday: Closed
            Thursday: Closed
            Friday: Closed
            Saturday: Closed
            Sunday: Closed
        """.trimIndent()
    ),
    TestCase(
        name = "Test when schedule is empty",
        schedule = emptyMap(),
        expected = """
            Monday: Closed
            Tuesday: Closed
            Wednesday: Closed
            Thursday: Closed
            Friday: Closed
            Saturday: Closed
            Sunday: Closed
        """.trimIndent()
    ),
    TestCase(
        name = "Test basic functionality, that minutes are included and seconds are ignored",
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(OPEN, 3900), ScheduleEvent(CLOSE, 7200)),
            TUESDAY to listOf(ScheduleEvent(OPEN, 3600), ScheduleEvent(CLOSE, 7200)),
            FRIDAY to listOf(ScheduleEvent(OPEN, 3640), ScheduleEvent(CLOSE, 7210)),
        ),
        expected = """
            Monday: 1:05 AM - 2 AM
            Tuesday: 1 AM - 2 AM
            Wednesday: Closed
            Thursday: Closed
            Friday: 1 AM - 2 AM
            Saturday: Closed
            Sunday: Closed
        """.trimIndent()
    ),
    TestCase(
        name = "Modified assignment example",
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(OPEN, 7200)),
            TUESDAY to listOf(
                ScheduleEvent(CLOSE, 10800),
                ScheduleEvent(OPEN, 36000),
                ScheduleEvent(CLOSE, 64800)
            ),
            WEDNESDAY to emptyList(),
            THURSDAY to listOf(
                ScheduleEvent(OPEN, 3600),
                ScheduleEvent(CLOSE, 7200),
                ScheduleEvent(OPEN, 37800),
                ScheduleEvent(CLOSE, 64800)
            ),
            FRIDAY to listOf(ScheduleEvent(OPEN, 36000)),
            SATURDAY to listOf(ScheduleEvent(CLOSE, 3600), ScheduleEvent(OPEN, 36000)),
            SUNDAY to listOf(
                ScheduleEvent(CLOSE, 3600),
                ScheduleEvent(OPEN, 43200),
                ScheduleEvent(CLOSE, 75600)
            )
        ),
        expected = """
            Monday: 2 AM - 3 AM
            Tuesday: 10 AM - 6 PM
            Wednesday: Closed
            Thursday: 1 AM - 2 AM, 10:30 AM - 6 PM
            Friday: 10 AM - 1 AM
            Saturday: 10 AM - 1 AM
            Sunday: 12 PM - 9 PM
        """.trimIndent()
    ),
    TestCase(
        name = "Sunday - Monday edge case",
        schedule = mapOf(
            MONDAY to listOf(ScheduleEvent(CLOSE, 3600)),
            SUNDAY to listOf(ScheduleEvent(OPEN, 43200))
        ),
        expected = """
            Monday: Closed
            Tuesday: Closed
            Wednesday: Closed
            Thursday: Closed
            Friday: Closed
            Saturday: Closed
            Sunday: 12 PM - 1 AM
        """.trimIndent()
    ),
)
