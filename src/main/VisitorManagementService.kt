package main

import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class VisitorManagementService {

    private val visitorStorage = ConcurrentHashMap<Long, Visitor>()

    fun getVisitorStorage():  ConcurrentHashMap<Long, Visitor> {
        return visitorStorage
    }

    fun enter(visitorId: Long, arrivedAt: LocalDate) {
        val visitor = visitorStorage[visitorId] ?: Visitor(visitorId)
        visitor.arrivalDates.add(arrivedAt)
        visitorStorage[visitorId] = visitor
    }

    fun leave(visitorId: Long, leftAt: LocalDate) {
        val visitor = visitorStorage[visitorId] ?: throw IllegalArgumentException()

        if (visitor.arrivalDates.isEmpty() || visitor.arrivalDates.last() > leftAt) {
            throw IllegalArgumentException()
        }
        visitor.departureDates.add(leftAt)
        visitorStorage[visitorId] = visitor
    }

    /**
     * If only visitors between two dates needed
     * like this relation (from >= arrivedAt >= leftAt >= to)
     */
    fun calculateAttendance(from: LocalDate, to: LocalDate): Int {
        if (to < from) {
            throw IllegalArgumentException()
        }
        return visitorStorage.values
                .count {
                    checkArrivalDates(it, from) && checkDepartureDates(it, to)
                }
    }

    private fun checkArrivalDates(visitor: Visitor, from: LocalDate): Boolean {
        return visitor.arrivalDates.any { it >= from }
    }

    private fun checkDepartureDates(visitor: Visitor, to: LocalDate): Boolean {
        return visitor.departureDates.any { it <= to }
    }

    /**
     * If relation is different:
     * 1. (arrivedAt >= from >= leftAt >= to)
     * 2. (arrivedAt >= from >= to >= leftAt)
     * 3. (from => arrivedAt >= to >= leftAt) etc.
     * this method should be used instead of @calculateAttendance
     */
    fun calculateAttendanceAlternatively(from: LocalDate, to: LocalDate): Int {
        if (to < from) {
            throw IllegalArgumentException()
        }
        return visitorStorage.values
                .count {
                    checkVisitorLeftBeforeDate(it, from) && checkVisitorArrivedBeforeDate(it, to)
                }
    }

    private fun checkVisitorLeftBeforeDate(visitor: Visitor, from: LocalDate): Boolean {
        return visitor.arrivalDates.size > visitor.departureDates.size || visitor.departureDates.any { it >= from }
    }

    private fun checkVisitorArrivedBeforeDate(visitor: Visitor, to: LocalDate): Boolean {
        return visitor.arrivalDates.any { it <= to }
    }
 }