package test

import main.model.DateRange
import main.model.Visitor
import main.services.VisitorManagementService
import main.services.impl.VisitorManagementServiceImpl
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VisitorManagementServiceTest {
    private lateinit var storage: ConcurrentHashMap<Long, Visitor>
    private lateinit var service: VisitorManagementService

    @Before
    fun setUp() {
        service = VisitorManagementServiceImpl()
        storage = service.visitorStorage
    }

    @Test
    fun `visitor enters first time successfully`() {
        assertNull(storage[1], "Visitor is already existed in system")
        service.enter(1, LocalDate.now())
        val firstVisitor = storage[1]
        assertNotNull(firstVisitor, "Visitor is not registered in system")
        assertTrue(firstVisitor!!.dateRanges.isNotEmpty(), "Date range is not set")
        assertNull(firstVisitor.dateRanges.last().to, "Visitor left the library")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `visitor tries to enter second time without leaving`() {
        val firstVisitor = Visitor(1, mutableListOf(DateRange(LocalDate.parse("2019-06-12"))))
        storage[1] = firstVisitor
        service.enter(1, LocalDate.now())
    }

    @Test
    fun `visitor enters second time successfully`() {
        val firstVisitor = Visitor(1, mutableListOf(DateRange(LocalDate.parse("2019-06-12"),
                LocalDate.parse("2019-06-13"))))
        storage[1] = firstVisitor
        service.enter(1, LocalDate.now())
        assertEquals(storage[1]!!.dateRanges.size,  2, "Visitor did not enter again")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non-registered visitor can not leave library`() {
        service.leave(1, LocalDate.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `leave fun should fail if visitor never arrives to library`() {
        val visitor = Visitor(1)
        storage[1] = visitor
        service.leave(1, LocalDate.now())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `leave fun should fail if leftAt date less than arrivedAt`() {
        val visitor = Visitor(1, mutableListOf(DateRange(LocalDate.now())))
        storage[1] = visitor
        service.leave(1, LocalDate.parse("2019-06-12"))
    }

    @Test
    fun `visitor leaves first time successfully`() {
        val visitor = Visitor(1, mutableListOf(DateRange(LocalDate.parse("2019-06-12"))))
        storage[1] = visitor
        service.leave(1, LocalDate.now())
        assertEquals(storage[1]!!.dateRanges.size, 1, "Incorrect count of date ranges")
        assertEquals(storage[1]!!.dateRanges.last().to, LocalDate.now(), "Visitor did not leave library")
    }

    @Test
    fun `visitor leaves second time successfully`() {
        val visitor = Visitor(1, mutableListOf(
                DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-13")),
                DateRange(LocalDate.parse("2019-06-17"))))
        storage[1] = visitor
        service.leave(1, LocalDate.now())
        assertEquals(storage[1]!!.dateRanges.size, 2, "Incorrect count of date ranges")
        assertEquals(storage[1]!!.dateRanges.last().to, LocalDate.now(), "Visitor did not leave library")
    }

    @Test
    fun `leave fun should not rewrite leftAt date`() {
        val visitor = Visitor(1, mutableListOf(
                DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-13"))))
        storage[1] = visitor
        service.leave(1, LocalDate.now())
        assertNotEquals(LocalDate.now(), storage[1]!!.dateRanges.last().to, "Dates have to be different")
    }

    @Test
    fun `calculate attendance when date range between input dates`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val visitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-13"))))
        storage[1] = visitor
        assertEquals(storage.size, 1, "Not only one visitor is stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-11"), LocalDate.now())
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance with open dateRangeTo`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")

        val firstVisitor = Visitor(1, mutableListOf(DateRange(LocalDate.parse("2019-06-12"))))
        storage[1] = firstVisitor
        val secondVisitor = Visitor(2, mutableListOf(DateRange(LocalDate.parse("2019-06-11"))))
        storage[2] = secondVisitor
        val thirdVisitor = Visitor(3, mutableListOf(DateRange(LocalDate.parse("2019-06-10"))))
        storage[3] = thirdVisitor

        assertEquals(storage.size, 3, "Not all visitors are stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-11"), LocalDate.now())
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance when dateRangeFrom less from less dateRangeTo less to`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val visitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-14"))))
        storage[1] = visitor
        assertEquals(storage.size, 1, "Not only one visitor is stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-13"), LocalDate.now())
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance when From less DateRangeFrom less To less DateRangeTo`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val visitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-13"), LocalDate.now())))
        storage[1] = visitor
        assertEquals(storage.size, 1, "Not only one visitor is stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-14"))
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance when dateRangeFrom less from less to less dateRangeTo`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val visitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-12"), LocalDate.now())))
        storage[1] = visitor
        assertEquals(storage.size, 1, "Not only one visitor is stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-13"), LocalDate.parse("2019-06-14"))
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance when dateRangeTo = from and dateRangeFrom = to`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val firstVisitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-13"))))
        storage[1] = firstVisitor
        val secondVisitor = Visitor(2,
                mutableListOf(DateRange(LocalDate.parse("2019-06-14"), LocalDate.parse("2019-06-15"))))
        storage[2] = secondVisitor
        assertEquals(storage.size, 2, "Not only current visitors are stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-13"), LocalDate.parse("2019-06-14"))
        assertEquals(count, storage.size, "Incorrect attendants count")
    }

    @Test
    fun `calculate attendance when date ranges are not intersected with input dates`() {
        assertTrue(storage.isEmpty(), "Visitors are stored")
        val firstVisitor = Visitor(1,
                mutableListOf(DateRange(LocalDate.parse("2019-06-12"), LocalDate.parse("2019-06-13"))))
        storage[1] = firstVisitor
        val secondVisitor = Visitor(2,
                mutableListOf(DateRange(LocalDate.parse("2019-06-16"), LocalDate.parse("2019-06-17"))))
        storage[2] = secondVisitor
        assertEquals(storage.size, 2, "Not only current visitors are stored")
        val count = service.calculateAttendance(LocalDate.parse("2019-06-14"), LocalDate.parse("2019-06-15"))
        assertEquals(count, 0, "Incorrect attendants count")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculate attendance should fail with incorrect input`() {
        service.calculateAttendance(LocalDate.parse("2019-06-15"), LocalDate.parse("2019-06-14"))
    }
}
