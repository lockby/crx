package com.crstlnz.komikchino

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crstlnz.komikchino.data.database.KomikDatabase
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryDao
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TodoDatabaseTest {

    private lateinit var readHistoryDao: ChapterHistoryDao
    private lateinit var db: KomikDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, KomikDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        readHistoryDao = db.getReadHistoryDao()
    }

    @After
    @Throws(IOException::class)
    fun deleteDb() {
        db.close()
    }

//    @Test
//    @Throws(Exception::class)
//    fun insertTest() = runBlocking {
//        val item = ReadHistoryItem(id = 1L, chapterId = 4, mangaId = 10)
//        readHistoryDao.insert(item)
//        val oneItem = readHistoryDao.getById(1)
//        assertEquals(oneItem?.id, 1L)
//        assertEquals(oneItem?.chapterId, 4)
//        assertEquals(oneItem?.mangaId, 10)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun deleteTest() = runBlocking {
//        val item = ReadHistoryItem(id = 1L, chapterId = 4, mangaId = 10)
//        readHistoryDao.insert(item)
//        val oneItem = readHistoryDao.getById(1)
//        assertEquals(oneItem?.id, 1L)
//        assertEquals(oneItem?.chapterId, 4)
//        assertEquals(oneItem?.mangaId, 10)
//        if (oneItem != null) {
//            readHistoryDao.delete(oneItem)
//        }
//        val da = readHistoryDao.getById(1)
//        assertEquals(da, null)
//    }
}