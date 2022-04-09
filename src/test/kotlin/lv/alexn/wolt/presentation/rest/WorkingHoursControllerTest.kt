package lv.alexn.wolt.presentation.rest

import lv.alexn.wolt.application.HumanReadableFormatter
import lv.alexn.wolt.configuration.IntegrationTest
import lv.alexn.wolt.extension.LogPredicate
import lv.alexn.wolt.extension.Logs
import lv.alexn.wolt.extension.Pattern
import lv.alexn.wolt.extension.body
import lv.alexn.wolt.extension.json
import lv.alexn.wolt.extension.status
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.reflect.jvm.jvmName

@IntegrationTest
class WorkingHoursControllerTest(
    private val mvc: MockMvc
) {

    @Nested
    inner class HappyPath {

        @Test
        fun `happy path - json is properly formatted`() {
            mvc.format(jsonRequest()).andExpect {
                status(HttpStatus.OK)
                body(
                    "Monday: Closed\n" +
                            "Tuesday: 10 AM - 6 PM\n" +
                            "Wednesday: Closed\n" +
                            "Thursday: 10:30 AM - 6 PM\n" +
                            "Friday: 10 AM - 1 AM\n" +
                            "Saturday: 10 AM - 1 AM\n" +
                            "Sunday: 12 PM - 9 PM"
                )
            }
        }

        @Test
        fun `happy path - json order of days is incomplete`() {
            val json = """
            {
              "monday": [
                {
                  "type": "open",
                  "value": 3600
                },
                {
                  "type": "close",
                  "value": 75600
                }
              ]
            }
        """.trimIndent()
            val expected =
                "Monday: 1 AM - 9 PM\nTuesday: Closed\nWednesday: Closed\nThursday: Closed\nFriday: Closed\nSaturday: Closed\nSunday: Closed"

            mvc.format(json).andExpect {
                status(HttpStatus.OK)
                body(expected)
            }
        }

        @Test
        fun `happy path - order of days is irrelevant`() {
            val json = """
            {
                "sunday":[{"type":"close","value":3600},{"type":"open","value":43200},{"type":"close","value":75600}],
                "monday":[],
                "saturday":[{"type":"close","value":3600},{"type":"open","value":36000}],
                "wednesday":[],
                "thursday":[{"type":"open","value":37800},{"type":"close","value":64800}],
                "friday":[{"type":"open","value":36000}],
                "tuesday":[{"type":"open","value":36000},{"type":"close","value":64800}]
            }
        """.trimIndent()

            mvc.format(json).andExpect {
                status(HttpStatus.OK)
                body(
                    "Monday: Closed\n" +
                            "Tuesday: 10 AM - 6 PM\n" +
                            "Wednesday: Closed\n" +
                            "Thursday: 10:30 AM - 6 PM\n" +
                            "Friday: 10 AM - 1 AM\n" +
                            "Saturday: 10 AM - 1 AM\n" +
                            "Sunday: 12 PM - 9 PM"
                )
            }
        }
    }

    @Nested
    inner class Errors {

        @Test
        fun `fail gracefully and log error when request is empty`(logs: Logs) {

            mvc.format("{}").andExpect {
                status(HttpStatus.OK)
                body("Monday: Closed\nTuesday: Closed\nWednesday: Closed\nThursday: Closed\nFriday: Closed\nSaturday: Closed\nSunday: Closed")
            }

            val predicate: LogPredicate = { it.loggerName == HumanReadableFormatter::class.jvmName }
            assertThat(logs.with(predicate = predicate, pattern = Pattern.SIMPLE))
                .containsExactly(
                    " INFO - Unable to format schedule - schedule is empty"
                )
        }
    }

    fun MockMvc.format(body: String) =
        post("/api/v1/format/human-readable") {
            json(body)
        }
}

private fun jsonRequest(): String =
    """
    {
        "monday":[],
        "tuesday":[{"type":"open","value":36000},{"type":"close","value":64800}],
        "wednesday":[],
        "thursday":[{"type":"open","value":37800},{"type":"close","value":64800}],
        "friday":[{"type":"open","value":36000}],
        "saturday":[{"type":"close","value":3600},{"type":"open","value":36000}],
        "sunday":[{"type":"close","value":3600},{"type":"open","value":43200},{"type":"close","value":75600}]
    }
    """.trimIndent()
