package com.github.skel2007.newporn.model

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import me.ivmg.telegram.entities.Message
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.util.KMongoUtil
import javax.inject.Inject

/**
 * @author skel
 */

data class Channel(val _id: Long, val url: String, val admins: List<Long>)

fun Message.toChannel(admins: List<Long>): Channel {
    return Channel(chat.id, chat.username!!, admins)
}

class ChannelsDao @Inject constructor(db: MongoDatabase) {

    internal val collection = db.getCollection<Channel>("channels")

    init {
        collection.ensureIndex(Sorts.ascending("admins"))
    }

    fun insertOrReplace(channel: Channel) {
        collection.replaceOne(KMongoUtil.idFilterQuery(channel._id), channel, ReplaceOptions().upsert(true))
    }

    fun find(channelId: Long): Channel? {
        return collection.findOneById(channelId)
    }

    fun findByAdminId(adminId: Long): List<Channel> {
        return collection.find(Filters.eq("admins", adminId)).sort(Sorts.ascending("_id")).toList()
    }
}
