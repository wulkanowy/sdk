package io.github.wulkanowy.api.notes

import io.github.wulkanowy.api.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Test


class NotesTest : BaseTest() {

    private val filled by lazy {
        getSnpRepo(NotesTest::class.java, "UwagiOsiagniecia-filled.html").getNotes().blockingGet()
    }

    private val empty by lazy {
        getSnpRepo(NotesTest::class.java, "UwagiOsiagniecia-empty.html").getNotes().blockingGet()
    }

    @Test
    fun getAllNotesTest() {
        assertEquals(3, filled.size)
        assertEquals(0, empty.size)
    }

    @Test
    fun getDateTest() {
        assertEquals(getDate(2017, 6, 6), filled[0].date)
        assertEquals(getDate(2016, 10, 1), filled[2].date)
    }

    @Test
    fun getTeacherTest() {
        assertEquals("Jan Kowalski [JK]", filled[0].teacher)
        assertEquals("Kochański Leszek [KL]", filled[2].teacher)
    }

    @Test
    fun getCategoryTest() {
        assertEquals("Zaangażowanie społeczne", filled[0].category)
        assertEquals("Zachowanie na lekcji", filled[2].category)
    }

    @Test
    fun getContentTest() {
        assertEquals("Pomoc przy pikniku charytatywnym", filled[0].content)
        assertEquals("Przeszkadzanie w prowadzeniu lekcji", filled[2].content)
    }
}