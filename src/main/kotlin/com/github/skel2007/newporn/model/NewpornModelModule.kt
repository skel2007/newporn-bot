package com.github.skel2007.newporn.model

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
import org.litote.kmongo.KMongo

/**
 * @author skel
 */
@Module
class NewpornModelModule {

    @Provides
    fun provideMongoClient(): MongoClient {
        val host = System.getenv("MONGO_HOST")
        return KMongo.createClient(host)
    }

    @Provides
    fun provideNewpornDb(client: MongoClient): MongoDatabase {
        return client.getDatabase("newporn")
    }

    @Provides
    fun providePostsDao(db: MongoDatabase): PostsDao {
        return PostsDao(db)
    }

    @Provides
    fun provideChannelsDao(db: MongoDatabase): ChannelsDao {
        return ChannelsDao(db)
    }
}
