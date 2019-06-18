package main.services.impl

import main.model.DateRange
import main.model.Visitor
import main.services.VisitorManagementService
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class VisitorManagementServiceImpl : VisitorManagementService {
    override var visitorStorage = ConcurrentHashMap<Long, Visitor>()

    override fun enter(visitorId: Long, arrivedAt: LocalDate) {
        when (visitorStorage[visitorId]) {
            null -> visitorStorage.putIfAbsent(visitorId, Visitor(visitorId, mutableListOf(DateRange(from = arrivedAt))))
            else -> visitorStorage.computeIfPresent(visitorId) { _, v ->
                v.also {
                    v.dateRanges.last().to
                            ?: throw IllegalArgumentException("Leaving date for visitor $visitorId should be registered at first")

                    v.dateRanges.add(DateRange(arrivedAt))
                }
            }
        }
    }

    override fun leave(visitorId: Long, leftAt: LocalDate) {
        val visitor = visitorStorage[visitorId]
                ?: throw IllegalArgumentException("Visitor $visitorId has to be already registered")

        synchronized(visitor) {
            checkVisitor(visitor, leftAt)
            if (visitor.dateRanges.last().to == null) {
                visitor.dateRanges.last().to = leftAt
            }
        }
    }

    private fun checkVisitor(visitor: Visitor, leftAt: LocalDate): Visitor {
        if (visitor.dateRanges.isEmpty()) {
            throw IllegalArgumentException("Visitor ${visitor.visitorId} never enters to library")
        }
        if (visitor.dateRanges.last().from > leftAt) {
            throw IllegalArgumentException("Incorrect input date $leftAt")
        }
        return visitor
    }

    /**
     * This method covers next relations between dates
     * 1. from <= dateRange.from <= dateRange.to <= to
     * 2. dateRange.from <= from <= dateRange.to <= to
     * 3. from <= dateRange.from <= to <= dateRange.to
     * 4. dateRange.from <= from <= to <= dateRange.to
     */
    override fun calculateAttendance(from: LocalDate, to: LocalDate): Int {
        if (to < from) {
            throw IllegalArgumentException("Incorrect to-date value $to")
        }
        return visitorStorage.values
                .count {
                    checkDateRange(it.dateRanges, from, to)
                }
    }

    private fun checkDateRange(dateRanges: List<DateRange>, from: LocalDate, to: LocalDate): Boolean {
        return dateRanges
                .any { it.from <= to && (it.to == null || from <= it.to) }
    }
}