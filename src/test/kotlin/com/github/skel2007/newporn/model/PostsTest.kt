package com.github.skel2007.newporn.model

import com.google.gson.Gson
import me.ivmg.telegram.entities.Message
import org.junit.jupiter.api.Test
import org.litote.kmongo.KMongo
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * @author skel
 */
internal class PostsTest {

    @Test
    fun toPost_simple() {
        val json = "{" +
                "\"message_id\":6," +
                "\"chat\":{" +
                    "\"id\":-1001186556327," +
                    "\"title\":\"newporn_tmp01\"," +
                    "\"username\":\"newporn_tmp01\"," +
                    "\"type\":\"channel\"" +
                "}," +
                "\"date\":1537176697," +
                "\"text\":\"qewdskfdsa\"}"

        val message = Gson().fromJson(json, Message::class.java)

        assertEquals(Post(
                PostId(-1001186556327, 6),
                "qewdskfdsa",
                listOf(),
                Instant.ofEpochSecond(1537176697)
        ), message.toPost())
    }

    @Test
    fun toPost_hashtags() {
        val json = "{" +
                "\"message_id\":8," +
                "\"chat\":{" +
                    "\"id\":-1001186556327," +
                    "\"title\":\"newporn_tmp01\"," +
                    "\"username\":\"newporn_tmp01\"," +
                    "\"type\":\"channel\"" +
                "}," +
                "\"date\":1537177479," +
                "\"edit_date\":1537177548," +
                "\"text\":\"saldfjaf\n#ht1\"," +
                "\"entities\":[" +
                    "{" +
                        "\"offset\":9," +
                        "\"length\":4," +
                        "\"type\":\"hashtag\"" +
                    "}" +
                "]}"

        val message = Gson().fromJson(json, Message::class.java)

        assertEquals(Post(
                PostId(-1001186556327, 8),
                "saldfjaf",
                listOf("#ht1"),
                Instant.ofEpochSecond(1537177479)
        ), message.toPost())
    }

    @Test
    fun toPost_noText() {
        val json = "{" +
                "\"message_id\":9," +
                "\"chat\":{" +
                    "\"id\":-1001186556327," +
                    "\"title\":\"newporn\"," +
                    "\"username\":\"newporn_tmp01\"," +
                    "\"type\":\"channel\"" +
                "}," +
                "\"date\":1537179149," +
                "\"new_chat_title\":\"newporn\"}"

        val message = Gson().fromJson(json, Message::class.java)

        assertNull(message.toPost())
    }

    @Test
    fun dao() {
        val postsDao = PostsDao(KMongo.createClient().getDatabase("newporn-test"))
        postsDao.collection.drop()

        val now = Instant.now()

        val postId11 = PostId(1, 1)
        val postId12 = PostId(1, 2)
        val postId21 = PostId(2, 1)

        assertNull(postsDao.find(postId11))
        assertNull(postsDao.find(postId12))
        assertNull(postsDao.find(postId21))

        val post11 = Post(postId11, "Text1", listOf(), now.minus(Duration.ofHours(10)))
        postsDao.insertOrUpdate(post11)

        val post21 = Post(postId21, "Text2", listOf(), now.minus(Duration.ofHours(6)))
        postsDao.insertOrUpdate(post21)

        assertEquals(post11, postsDao.find(postId11))
        assertNull(postsDao.find(postId12))
        assertEquals(post21, postsDao.find(postId21))

        val editedPost12 = Post(postId11, "Edited Text1", listOf("#ht1"), post11.date)
        postsDao.insertOrUpdate(editedPost12)

        assertEquals(editedPost12, postsDao.find(postId11))
        assertNull(postsDao.find(postId12))
        assertEquals(post21, postsDao.find(postId21))

        val post12 = Post(postId12, "Text Text", listOf("#ht1", "#ht2"), now.minus(Duration.ofHours(4)))
        postsDao.insertOrUpdate(post12)

        assertEquals(editedPost12, postsDao.find(postId11))
        assertEquals(post12, postsDao.find(postId12))
        assertEquals(post21, postsDao.find(postId21))

        assertEquals(listOf(editedPost12, post12),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(12)), now.minus(Duration.ofHours(1))))
        assertEquals(listOf(post12),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(8)), now.minus(Duration.ofHours(1))))
        assertEquals(listOf(editedPost12),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(12)), now.minus(Duration.ofHours(8))))
        assertEquals(listOf(),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(20)), now.minus(Duration.ofHours(12))))
        assertEquals(listOf(),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1))))

        assertEquals(listOf(editedPost12, post12),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(12)), now.minus(Duration.ofHours(1)), "#ht1"))
        assertEquals(listOf(post12),
                     postsDao.findByChannelId(1, now.minus(Duration.ofHours(12)), now.minus(Duration.ofHours(1)), "#ht2"))
    }
}
