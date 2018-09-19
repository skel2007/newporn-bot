package com.github.skel2007.newporn.model

import org.junit.jupiter.api.Test
import org.litote.kmongo.KMongo
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * @author skel
 */
internal class ChannelsTest {

    @Test
    fun dao() {
        val channelsDao = ChannelsDao(KMongo.createClient().getDatabase("newporn-test"))
        channelsDao.collection.drop()

        assertNull(channelsDao.find(1))
        assertNull(channelsDao.find(2))
        assertNull(channelsDao.find(3))

        val channel1 = Channel(1, "channel1", listOf(1, 2))
        channelsDao.insertOrReplace(channel1)

        val channel2 = Channel(2, "channel2", listOf(1, 3))
        channelsDao.insertOrReplace(channel2)

        val channel3 = Channel(3, "channel3", listOf(4))
        channelsDao.insertOrReplace(channel3)

        assertEquals(channel1, channelsDao.find(1))
        assertEquals(channel2, channelsDao.find(2))
        assertEquals(channel3, channelsDao.find(3))

        val editedChannel3 = Channel(3, "channel3", listOf(3))
        channelsDao.insertOrReplace(editedChannel3)

        assertEquals(channel1, channelsDao.find(1))
        assertEquals(channel2, channelsDao.find(2))
        assertEquals(editedChannel3, channelsDao.find(3))

        assertEquals(listOf(channel1, channel2), channelsDao.findByAdminId(1))
        assertEquals(listOf(channel1), channelsDao.findByAdminId(2))
        assertEquals(listOf(channel2, editedChannel3), channelsDao.findByAdminId(3))
        assertEquals(listOf(), channelsDao.findByAdminId(4))
    }
}
