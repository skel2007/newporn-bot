package com.github.skel2007.newporn.model

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import me.ivmg.telegram.entities.Message
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.inject.Inject

/**
 * @author skel
 */

data class PostId(val channelId: Long, val messageId: Long)

data class Post(val _id: PostId, val title: String, val hashtags: List<String>, val date: Instant)

fun Message.toPost(): Post? {
    return text?.let { text ->
        val postId = PostId(chat.id, messageId)
        val title = text.lineSequence().first()

        val hashtags = entities
                ?.asSequence()
                ?.filter { it.type == "hashtag" }
                ?.map { text.substring(it.offset, it.offset + it.length) }
                ?.toList()
                ?: listOf()

        val date = Instant.ofEpochSecond(date.toLong())

        return Post(postId, title, hashtags, date)
    }
}

class PostsDao @Inject constructor(db: MongoDatabase) {

    private val logger = LoggerFactory.getLogger(PostsDao::class.java)

    internal val collection = db.getCollection<Post>("posts")

    init {
        collection.ensureIndex(Sorts.ascending("_id.channelId"))
        collection.ensureIndex(Sorts.ascending("date"))
        collection.ensureIndex(Sorts.ascending("hashtags"))
    }

    fun insertOrUpdate(post: Post) {
        logger.info("Going to save $post to db")
        collection.replaceOne(idFilterQuery(post._id), post, ReplaceOptions().upsert(true))
    }

    fun find(postId: PostId): Post? {
        return collection.findOneById(postId)
    }

    fun findByChannelId(channelId: Long, from: Instant, to: Instant, hashtag: String? = null): List<Post> {
        val filters = mutableListOf(
                Filters.eq("_id.channelId", channelId),
                Filters.gte("date", from),
                Filters.lte("date", to))

        hashtag?.let {
            filters.add(Filters.eq("hashtags", it))
        }

        val sort = Sorts.ascending("date")

        return collection.find(Filters.and(filters)).sort(sort).toList()
    }
}
