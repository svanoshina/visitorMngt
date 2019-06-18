package main.model

import java.time.LocalDate

data class DateRange(
        var from: LocalDate,
        var to: LocalDate? = null
)