package com.github.skel2007.newporn

import com.github.skel2007.newporn.model.NewpornModelModule
import dagger.Component

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

@Component(modules = arrayOf(
        NewpornModelModule::class,
        NewpornTelegramModule::class))
interface NewpornApp {
    fun bot(): NewpornBot
}
