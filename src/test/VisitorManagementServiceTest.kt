package test

import main.Visitor
import main.VisitorManagementService
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VisitorManagementServiceTest {
    private lateinit var storage: ConcurrentHashMap<Long, Visitor>
    private lateinit var service: VisitorManagementService
    private val secondVisitor = Visitor(2, TreeSet(setOf(LocalDate.parse("2019-06-12"))))
    private var thirdVisitor = Visitor(3, TreeSet(setOf(LocalDate.parse("2019-06-12"))))

    @Before
    fun setUp() {
        service = VisitorManagementService()
        storage = service.getVisitorStorage()
        val arrivedDatesSet = TreeSet<LocalDate>()
        arrivedDatesSet.add(LocalDate.parse("2019-06-12"))
        val leftDatesSet = TreeSet<LocalDate>()
        leftDatesSet.add(LocalDate.now())
        storage[1] = Visitor(1,
                TreeSet(setOf(LocalDate.parse("2019-06-12"))),
                TreeSet(setOf(LocalDate.now())))
    }

    @Test
    fun `check new visitor enters successfully`() {
        assertNull(storage[2], "main.Visitor is already existed in system")
        service.enter(2, LocalDate.now())
        assertNotNull(storage[2], "main.Visitor is not registered in system")
        val visitor = storage[2]!!
        assertTrue(visitor.departureDates.isEmpty(), "main.Visitor left the library")
    }

    @Test
    fun `check registered visitor enters successfully`() {
        assertNotNull(storage[1], "main.Visitor is not registered in system")
        service.enter(1, LocalDate.now())
        assertTrue(storage[1]!!.arrivalDates.size > 1, "main.Visitor has not arrived yet")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `leave fun should fail if visitor never arrives to library`() {
        val visitor = Visitor(3)
        storage[3] = visitor
        service.leave(3, LocalDate.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `check visitor can not leave before arriving`() {
        val newVisitor = Visitor(4)
        storage[4] = newVisitor
        service.leave(4, LocalDate.parse("2019-06-12"))
    }

    @Test
    fun `check visitor leaves successfully`(){
        storage[3] = thirdVisitor
        service.leave(3, LocalDate.now())
        assertTrue(storage[3]!!.departureDates.isNotEmpty(), "main.Visitor did not leave library")
    }

    @Test
    fun `calculate all attendants`() {
        secondVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        thirdVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.now())))
        storage[2] = secondVisitor
        storage[3] = thirdVisitor
        val count = service.calculateAttendance(LocalDate.parse("2019-06-10"), LocalDate.MAX)
        assertEquals(count, storage.size)
    }

    @Test
    fun `calculate attendants`() {
        secondVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        val newVisitor = Visitor(4,
                TreeSet(setOf(LocalDate.parse("2019-06-13"))),
                TreeSet(setOf(LocalDate.now())))
        storage[2] = secondVisitor
        storage[4] = newVisitor
        val count = service.calculateAttendance(LocalDate.parse("2019-06-11"),
                LocalDate.parse("2019-06-13"))
        assertEquals(count, 1)
    }

    @Test
    fun `check visitor not left yet`() {
        secondVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        storage[2] = secondVisitor
        storage[3] = thirdVisitor
        val count = service.calculateAttendance(LocalDate.parse("2019-06-12"), LocalDate.now())
        assertEquals(count, 2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `check input is correct`() {
        secondVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        thirdVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.now())))
        storage[2] = secondVisitor
        storage[3] = thirdVisitor
        service.calculateAttendance(LocalDate.now(), LocalDate.parse("2019-06-12"))
    }

    @Test
    fun `calculate attendants alternatively`() {
        val oldVisitor = Visitor(4,
                TreeSet(setOf(LocalDate.parse("2019-06-10"))),
                TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        val newVisitor = Visitor(5,
                TreeSet(setOf(LocalDate.parse("2019-06-14"))),
                TreeSet(setOf(LocalDate.now())))
        storage[4] = oldVisitor
        storage[5] = newVisitor

        val count = service.calculateAttendanceAlternatively(LocalDate.parse("2019-06-11"),
                LocalDate.parse("2019-06-13"))
        assertEquals(count, 2)
    }

    @Test
    fun `check visitor not left yet alternatively`() {
        secondVisitor.departureDates.addAll(TreeSet(setOf(LocalDate.parse("2019-06-13"))))
        storage[2] = secondVisitor
        storage[3] = thirdVisitor
        val count = service.calculateAttendanceAlternatively(LocalDate.parse("2019-06-12"), LocalDate.now())
        assertEquals(count, storage.size)
    }
}
