package lv.alexn.wolt.application

import lv.alexn.wolt.application.TimeType.Close
import lv.alexn.wolt.application.TimeType.Open
import lv.alexn.wolt.application.exception.InvalidScheduleStructure
import lv.alexn.wolt.extension.KArgumentsProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class HumanReadableFormatterTest {

    @Nested
    inner class HappyPath {

        @ParameterizedTest
        @ArgumentsSource(FormatArgumentsProvider::class)
        fun `happy path - format`(args: TestCase) {

            val formatter = HumanReadableFormatter()
            assertThat(formatter.formatSchedule(args.schedule)).isEqualTo(args.expected)
        }
    }

    @Nested
    inner class Exception {

        @ParameterizedTest
        @ArgumentsSource(ExceptionCaseProvider::class)
        fun `test exception being thrown on invalid structure`(args: TestCase) {

            val formatter = HumanReadableFormatter()
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
    val expected: String
)

private class ExceptionCaseProvider : KArgumentsProvider<TestCase>(
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Open, 3900), OperatingHourEvent(Open, 7200))
        ),
        "Invalid schedule structure -- last element should be 'Close', 'Open' encountered."
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Close, 3900), OperatingHourEvent(Close, 7200))
        ),
        "Invalid schedule structure -- first element should be 'Open', 'Close' encountered."
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Open, 3900))
        ),
        "Invalid schedule structure -- last element should be 'Close', 'Open' encountered."
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Close, 3900))
        ),
        "Invalid schedule structure -- first element should be 'Open', 'Close' encountered."
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Open, 3600)),
            friday = listOf(OperatingHourEvent(Close, 43200))
        ),
        "Invalid schedule structure -- opening times spanning multiple days are not supported."
    ),
)

private class FormatArgumentsProvider : KArgumentsProvider<TestCase>(
    TestCase( // Test when schedule is empty
        schedule = Schedule(),
        expected = "Monday: Closed\nTuesday: Closed\nWednesday: Closed\nThursday: Closed\nFriday: Closed\nSaturday: Closed\nSunday: Closed"
    ),
    TestCase( // Test basic functionality, that minutes are included and seconds are ignored
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Open, 3900), OperatingHourEvent(Close, 7200)),
            tuesday = listOf(OperatingHourEvent(Open, 3600), OperatingHourEvent(Close, 7200)),
            friday = listOf(OperatingHourEvent(Open, 3640), OperatingHourEvent(Close, 7210)),
        ),
        expected = "Monday: 1:05 AM - 2 AM\nTuesday: 1 AM - 2 AM\nWednesday: Closed\nThursday: Closed\nFriday: 1 AM - 2 AM\nSaturday: Closed\nSunday: Closed"
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Open, 7200)),
            tuesday = listOf(
                OperatingHourEvent(Close, 10800),
                OperatingHourEvent(Open, 36000),
                OperatingHourEvent(Close, 64800)
            ),
            wednesday = emptyList(),
            thursday = listOf(
                OperatingHourEvent(Open, 3600),
                OperatingHourEvent(Close, 7200),
                OperatingHourEvent(Open, 37800),
                OperatingHourEvent(Close, 64800)
            ),
            friday = listOf(OperatingHourEvent(Open, 36000)),
            saturday = listOf(OperatingHourEvent(Close, 3600), OperatingHourEvent(Open, 36000)),
            sunday = listOf(
                OperatingHourEvent(Close, 3600),
                OperatingHourEvent(Open, 43200),
                OperatingHourEvent(Close, 75600)
            )
        ),
        expected = "Monday: 2 AM - 3 AM\nTuesday: 10 AM - 6 PM\nWednesday: Closed\nThursday: 1 AM - 2 AM, 10:30 AM - 6 PM\nFriday: 10 AM - 1 AM\nSaturday: 10 AM - 1 AM\nSunday: 12 PM - 9 PM"
    ),
    TestCase(
        schedule = Schedule(
            monday = listOf(OperatingHourEvent(Close, 3600)),
            sunday = listOf(OperatingHourEvent(Open, 43200))
        ),
        expected = "Monday: Closed\nTuesday: Closed\nWednesday: Closed\nThursday: Closed\nFriday: Closed\nSaturday: Closed\nSunday: 12 PM - 1 AM"
    ),
)
