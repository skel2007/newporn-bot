package com.github.skel2007.newporn.model

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import dagger.Module
import dagger.Provides
import org.litote.kmongo.KMongo
import javax.inject.Singleton

/**
 * @author skel
 */
@Module
class NewpornModelModule {

    @Provides
    @Singleton
    fun provideMongoClient(): MongoClient {
        val host = System.getenv("MONGO_HOST")
        return KMongo.createClient(host)
    }

    @Provides
    @Singleton
    fun provideNewpornDb(client: MongoClient): MongoDatabase {
        return client.getDatabase("newporn")
    }

    @Provides
    @Singleton
    fun providePostsDao(db: MongoDatabase): PostsDao {
        return PostsDao(db)
    }

    @Provides
    @Singleton
    fun provideChannelsDao(db: MongoDatabase): ChannelsDao {
        return ChannelsDao(db)
    }
}
