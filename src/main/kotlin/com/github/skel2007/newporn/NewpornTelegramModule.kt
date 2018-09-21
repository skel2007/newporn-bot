package com.github.skel2007.newporn

import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named

/**
 * @author skel
 */
@Module
class NewpornTelegramModule {

    @Provides
    @Named("botToken")
    fun provideBotToken(): String {
        return File(System.getenv("BOT_TOKEN_FILE")).readText()
    }
}
