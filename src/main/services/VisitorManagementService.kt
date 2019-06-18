package main.services

import main.model.Visitor
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

interface VisitorManagementService {
    var visitorStorage: ConcurrentHashMap<Long, Visitor>

    fun enter(visitorId: Long, arrivedAt: LocalDate)

    fun leave(visitorId: Long, leftAt: LocalDate)

    fun calculateAttendance(from: LocalDate, to: LocalDate): Int
}