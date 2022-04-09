package lv.alexn.wolt.presentation.rest

import lv.alexn.wolt.application.Schedule
import lv.alexn.wolt.application.ScheduleFormatter
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class WorkingHoursController(private val scheduleFormatter: ScheduleFormatter) {

    @PostMapping("/format/human-readable")
    fun formatHumanReadable(@RequestBody input: Schedule): String = scheduleFormatter.formatSchedule(input)
}
