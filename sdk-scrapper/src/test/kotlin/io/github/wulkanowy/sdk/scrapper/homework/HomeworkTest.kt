package io.github.wulkanowy.sdk.scrapper.homework

import io.github.wulkanowy.sdk.scrapper.BaseLocalTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeworkTest : BaseLocalTest() {

    private val homework by lazy {
        runBlocking {
            getStudentRepo(HomeworkTest::class.java, "Homework.json").getHomework(
                startDate = getLocalDate(2018, 10, 1),
                endDate = getLocalDate(2018, 10, 2),
            )
        }
    }

    @Test
    fun getHomeworkList() {
        assertEquals(4, homework.size)
    }

    @Test
    fun getHomeworkWithDifferentDate() {
        with(homework[0]) {
            assertEquals(getDate(2018, 10, 1), date)
            assertEquals(getDate(2018, 9, 25), entryDate)
            assertEquals("Naprawa komputera", subject)
            assertEquals("Test diagnozujący", content)
            assertEquals("Mickiewicz Adam", teacher)
            assertEquals("MA", teacherSymbol)
        }
    }

    @Test
    fun getHomework() {
        with(homework[1]) {
            assertEquals(getDate(2018, 10, 1), date)
            assertEquals(getDate(2018, 9, 25), entryDate)
            assertEquals("Naprawa komputera", subject)
            assertEquals("Test diagnozujący", content)
            assertEquals("Mickiewicz Adam", teacher)
            assertEquals("MA", teacherSymbol)
        }
    }

    @Test
    fun getHomeworkWithAttachments() {
        with(homework[2]) {
            assertEquals(getDate(2018, 10, 1), date)
            assertEquals(getDate(2018, 9, 25), entryDate)
            assertEquals("Techniki biurowe", subject)
            assertEquals("Zadania egzaminacyjne:\nstr 231 \nstr 254", content)
            assertEquals("Mateusz Kowal", teacher)
            assertEquals("MK", teacherSymbol)
            assertEquals(2, attachments.size)
            assertEquals("<a href=\"https://wulkanowy.github.io/\" target=\"_blank\">Strona główna Wulkanowego</a>", attachments[0].html)
            assertEquals("<a href=\"https://github.com/wulkanowy/wulkanowy/\" target=\"_blank\">Repozytorium kodu</a>", attachments[1].html)
            with(_attachments[0]) {
                assertEquals("https://wulkanowy.github.io/", first)
                assertEquals("Strona główna Wulkanowego", second)
            }
        }
    }

    @Test
    fun getHomeworkWithHtml() {
        with(homework[3]) {
            assertEquals(getDate(2018, 10, 2), date)
            assertEquals(getDate(2018, 10, 1, 18, 42, 32), entryDate)
            assertEquals("Matematyka", subject)
            assertEquals("OneNote -> Biblioteka zawartości -> 4_matura -> praca domowa - zadania maturalne", content)
            assertEquals("Borowska Agnieszka", teacher)
            assertEquals("BO", teacherSymbol)
            assertEquals(0, attachments.size)
        }
    }
}
