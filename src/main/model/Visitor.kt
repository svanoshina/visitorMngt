package main.model

data class Visitor(
        val visitorId: Long,
        val dateRanges: MutableList<DateRange> = mutableListOf()
)