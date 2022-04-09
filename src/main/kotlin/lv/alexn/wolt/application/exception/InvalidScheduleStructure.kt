package lv.alexn.wolt.application.exception

import lv.alexn.wolt.application.Schedule

class InvalidScheduleStructure(override val message: String, val schedule: Schedule) : RuntimeException(message)
