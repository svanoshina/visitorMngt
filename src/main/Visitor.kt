package main

import java.time.LocalDate
import java.util.*

data class Visitor(
        val visitorId: Long,
        val arrivalDates: TreeSet<LocalDate> = TreeSet(),
        val departureDates: TreeSet<LocalDate> = TreeSet()
)