package com.github.skel2007.newporn.model

import com.github.skel2007.newporn.NewpornBot.Companion.interval
import com.github.skel2007.newporn.NewpornBot.Companion.zoneId
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 * @author skel
 */
class NewpornBotTest {

    private fun String.toInstant(): Instant {
        return ZonedDateTime.of(LocalDateTime.parse(this), zoneId).toInstant()
    }

    @Test
    fun interval() {
        val thisWeek = Pair("2018-09-17T00:00:00".toInstant(), "2018-09-24T00:00:00".toInstant());
        val previousWeek = Pair("2018-09-10T00:00:00".toInstant(), "2018-09-17T00:00:00".toInstant());

        assertEquals(previousWeek, interval("2018-09-16T08:00:00".toInstant()))
        assertEquals(previousWeek, interval("2018-09-17T08:00:00".toInstant()))
        assertEquals(previousWeek, interval("2018-09-18T08:00:00".toInstant()))
        assertEquals(previousWeek, interval("2018-09-19T08:00:00".toInstant()))
        assertEquals(thisWeek, interval("2018-09-20T08:00:00".toInstant()))
        assertEquals(thisWeek, interval("2018-09-21T08:00:00".toInstant()))
        assertEquals(thisWeek, interval("2018-09-22T08:00:00".toInstant()))
        assertEquals(thisWeek, interval("2018-09-23T08:00:00".toInstant()))
        assertEquals(thisWeek, interval("2018-09-24T08:00:00".toInstant()))
    }
}
