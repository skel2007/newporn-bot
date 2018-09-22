package com.github.skel2007.newporn

import com.github.skel2007.newporn.model.NewpornModelModule
import dagger.Component
import javax.inject.Singleton

/**
 * @author skel
 */

fun main(args: Array<String>) {
    val app = DaggerNewpornApp.builder()
            .newpornModelModule(NewpornModelModule())
            .newpornTelegramModule(NewpornTelegramModule())
            .build()

    app.bot().startPolling()
}

@Component(modules = [NewpornModelModule::class, NewpornTelegramModule::class])
@Singleton
interface NewpornApp {
    fun bot(): NewpornBot
}
