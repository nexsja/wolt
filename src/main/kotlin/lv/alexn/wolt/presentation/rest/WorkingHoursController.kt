package lv.alexn.wolt.presentation.rest

import lv.alexn.wolt.application.HumanReadableFormatter
import lv.alexn.wolt.application.Schedule
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class WorkingHoursController(private val formatter: HumanReadableFormatter) {

    @PostMapping("/format/human-readable")
    fun formatHumanReadable(@RequestBody input: Schedule): String = formatter.formatSchedule(input)
}
