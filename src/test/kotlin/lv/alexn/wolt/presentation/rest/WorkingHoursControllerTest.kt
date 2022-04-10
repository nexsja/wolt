package lv.alexn.wolt.presentation.rest

import lv.alexn.wolt.IntegrationTest
import lv.alexn.wolt.extension.body
import lv.alexn.wolt.extension.json
import lv.alexn.wolt.extension.status
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.OK
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@IntegrationTest
class WorkingHoursControllerTest(
    private val mvc: MockMvc
) {

    @Nested
    inner class HappyPath {

        @Test
        fun `happy path - json is properly formatted`() {
            val json = """
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
            mvc.format(json).andExpect {
                status(OK)
                body(
                    """
                    Monday: Closed
                    Tuesday: 10 AM - 6 PM
                    Wednesday: Closed
                    Thursday: 10:30 AM - 6 PM
                    Friday: 10 AM - 1 AM
                    Saturday: 10 AM - 1 AM
                    Sunday: 12 PM - 9 PM
                    """.trimIndent()
                )
            }
        }

        @Test
        fun `happy path - json order of days is incomplete`() {
            val json = """
            {
              "monday": [{"type": "open", "value": 3600}, {"type": "close", "value": 75600}]
            }
            """.trimIndent()
            val expected = """
                Monday: 1 AM - 9 PM
                Tuesday: Closed
                Wednesday: Closed
                Thursday: Closed
                Friday: Closed
                Saturday: Closed
                Sunday: Closed
            """.trimIndent()

            mvc.format(json).andExpect {
                status(OK)
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
                status(OK)
                body(
                    """
                    Monday: Closed
                    Tuesday: 10 AM - 6 PM
                    Wednesday: Closed
                    Thursday: 10:30 AM - 6 PM
                    Friday: 10 AM - 1 AM
                    Saturday: 10 AM - 1 AM
                    Sunday: 12 PM - 9 PM
                    """.trimIndent()
                )
            }
        }
    }

    @Nested
    inner class Errors {

        @Test
        fun `fail gracefully when request is empty`() {

            mvc.format("{}").andExpect {
                status(OK)
                body(
                    """
                    Monday: Closed
                    Tuesday: Closed
                    Wednesday: Closed
                    Thursday: Closed
                    Friday: Closed
                    Saturday: Closed
                    Sunday: Closed
                    """.trimIndent()
                )
            }
        }
    }

    fun MockMvc.format(body: String) =
        post("/api/v1/format/human-readable") {
            json(body)
        }
}
