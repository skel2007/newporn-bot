package com.github.skel2007

import me.ivmg.telegram.bot
import org.litote.kmongo.KMongo
import java.io.File

/**
 * @author skel
 */
fun main(args: Array<String>) {
    val host = System.getenv("MONGO_HOST")
    val client = KMongo.createClient(host)
    val db = client.getDatabase("newporn")

    println("Connected to db ${db.name}")

    val botToken = File(System.getenv("BOT_TOKEN_FILE")).readText()
    val bot = bot {
        token = botToken
    }

    println("Created bot")

    bot.startPolling()
}
