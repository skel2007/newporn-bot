package com.github.skel2007.newporn

import com.github.skel2007.newporn.model.ChannelsDao
import com.github.skel2007.newporn.model.PostsDao
import com.github.skel2007.newporn.model.toChannel
import com.github.skel2007.newporn.model.toPost
import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.channel
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.Message
import me.ivmg.telegram.entities.ParseMode
import me.ivmg.telegram.entities.Update
import me.ivmg.telegram.network.fold
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Named

/**
 * @author skel
 */
class NewpornBot @Inject constructor(
        @Named("botToken")
        private val botToken: String,
        private val postsDao: PostsDao,
        private val channelsDao: ChannelsDao)
{
    fun startPolling() {
        val bot = bot {
            token = botToken

            dispatch {
                channel(this@NewpornBot::channelPost)
                command("start", this@NewpornBot::start)
                command("top", this@NewpornBot::top)
                callbackQuery("channel_", this@NewpornBot::topChannel)
                callbackQuery("hashtag_", this@NewpornBot::topChannelHashtag)
            }
        }

        bot.startPolling()
    }

    private fun channelPost(bot: Bot, update: Update) {
        val block: (Message) -> Unit = { channelPost ->
            channelPost.toPost()?.let { post ->
                postsDao.insertOrUpdate(post)

                bot.getChatAdministrators(channelPost.chat.id)
                        .fold({ resp ->
                                  resp?.result?.map { it.user.id }?.let { admins ->
                                      channelsDao.insertOrReplace(
                                              channelPost.toChannel(admins))
                                  }
                              })
            }
        }

        update.channelPost?.let(block)
        update.editedChannelPost?.let(block)
    }

    private fun start(bot: Bot, update: Update) {
        bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = "Приветствую тебя, моя госпожа")
    }

    private fun top(bot: Bot, update: Update) {
        val userId = update.message!!.from!!.id
        val chatId = update.message!!.chat.id

        val channels = channelsDao.findByAdminId(userId)
        if (channels.isEmpty()) {
            bot.sendMessage(
                    chatId = chatId,
                    text = "К сожалению, у меня нет постов ни с одного канала, моя госпожа")

            return
        }

        val buttons = channels.map { channel ->
            listOf(InlineKeyboardButton(
                    text = channel.url,
                    callbackData = "channel_${channel._id}"))
        }

        bot.sendMessage(
                chatId = chatId,
                text = "Выбери канал, моя госпожа",
                replyMarkup = InlineKeyboardMarkup(buttons))
    }

    private fun topChannel(bot: Bot, update: Update) {
        val chatId = update.callbackQuery!!.message!!.chat.id
        val channelId = update.callbackQuery!!.data.substring("channel_".length).toLong()

        val interval = interval(Instant.now())
        val hashtags = postsDao.findByChannelId(channelId, interval.first, interval.second)
                .asSequence()
                .flatMap { post -> post.hashtags.asSequence().map { Pair(it, post) } }
                .groupingBy { it.first }
                .eachCount()
                .entries
                .asSequence()
                .sortedByDescending { it.value }
                .take(5)
                .toList()

        if (hashtags.isEmpty()) {
            bot.sendMessage(
                    chatId = chatId,
                    text = "К сожалению, у меня нет статистики с этого канала, моя госпожа")

            return
        }

        val buttons = hashtags.map { hashtag ->
            listOf(InlineKeyboardButton(
                    text = "${hashtag.key}: ${hashtag.value}",
                    callbackData = "hashtag_${hashtag.key}_$channelId"))
        }

        bot.sendMessage(
                chatId = chatId,
                text = "Выбери хэштег, моя госпожа",
                replyMarkup = InlineKeyboardMarkup(buttons))
    }

    private fun topChannelHashtag(bot: Bot, update: Update) {
        val data = update.callbackQuery!!.data
        val i = data.lastIndexOf("_")

        val hashtag = data.substring("hashtag_".length, i)
        val channelId = data.substring(i + 1).toLong()

        channelsDao.find(channelId)?.let { channel ->
            val interval = interval(Instant.now())

            val posts = postsDao.findByChannelId(channelId, interval.first, interval.second, hashtag)
            val text = posts
                    .asSequence()
                    .map { "• [${it.title}](https://t.me/${channel.url}/${it._id.messageId})" }
                    .joinToString("\n")

            bot.sendMessage(
                    chatId = update.callbackQuery!!.message!!.chat.id,
                    text = "*$hashtag*\n$text",
                    parseMode = ParseMode.MARKDOWN,
                    disableWebPagePreview = true)
        }
    }

    companion object {

        internal val zoneId = ZoneId.of("Europe/Moscow")

        fun interval(now: Instant): Pair<Instant, Instant> {
            val today = LocalDateTime.ofInstant(now, zoneId).toLocalDate().atStartOfDay(zoneId)

            var start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            if (today.dayOfWeek.value < 4) {
                start = start.minusWeeks(1)
            }

            val end = start.plusWeeks(1)

            return Pair(start.toInstant(), end.toInstant())
        }
    }
}
