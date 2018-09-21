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
import me.ivmg.telegram.network.fold
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

/**
 * @author skel
 */
class NewpornBot @Inject constructor(
        @Named("botToken") botToken: String,
        postsDao: PostsDao,
        channelsDao: ChannelsDao)
{
    private val bot: Bot

    init {
        bot = bot {
            token = botToken

            dispatch {
                channel { bot, update ->
                    val block: (Message) -> Unit = { channelPost ->
                        channelPost.toPost()?.let { post ->
                            postsDao.insertOrUpdate(post)

                            bot.getChatAdministrators(channelPost.chat.id).fold({ resp ->
                                resp?.result?.map { it.user.id }?.let { admins ->
                                    channelsDao.insertOrReplace(channelPost.toChannel(admins))
                                }
                            })
                        }
                    }

                    update.channelPost?.let(block)
                    update.editedChannelPost?.let(block)
                }

                command("top") { bot, update, _ ->
                    val userId = update.message!!.from!!.id

                    val channels = channelsDao.findByAdminId(userId)
                    val buttons = channels.map { channel ->
                        listOf(InlineKeyboardButton(
                                text = channel.url,
                                callbackData = "channel_${channel._id}"))
                    }

                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            text = "Выберите канал",
                            replyMarkup = InlineKeyboardMarkup(buttons))
                }

                callbackQuery("channel_") { bot, update ->
                    val channelId = update.callbackQuery!!.data.substring("channel_".length).toLong()

                    val to = Instant.now()
                    val from = to.minus(Duration.ofHours(100_500)) // TODO

                    val hashtags = postsDao.findByChannelId(channelId, from, to)
                            .asSequence()
                            .flatMap { post -> post.hashtags.asSequence().map { Pair(it, post) } }
                            .groupingBy { it.first }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .take(5)

                    val buttons = hashtags.map { hashtag ->
                        listOf(InlineKeyboardButton(
                                text = "${hashtag.key}: ${hashtag.value}",
                                callbackData = "hashtag_${hashtag.key}_$channelId"))
                    }

                    bot.sendMessage(
                            chatId = update.callbackQuery!!.message!!.chat.id,
                            text = "Выберите хэштег",
                            replyMarkup = InlineKeyboardMarkup(buttons))
                }

                callbackQuery("hashtag_") { bot, update ->
                    val data = update.callbackQuery!!.data
                    val i = data.lastIndexOf("_")

                    val hashtag = data.substring("hashtag_".length, i)
                    val channelId = data.substring(i + 1).toLong()

                    channelsDao.find(channelId)?.let { channel ->
                        val to = Instant.now()
                        val from = to.minus(Duration.ofHours(100_500)) // TODO

                        val posts = postsDao.findByChannelId(channelId, from, to, hashtag)
                        val text = posts
                                .map { "• [${it.title}](https://t.me/${channel.url}/${it._id.messageId})" }
                                .joinToString("\n")

                        bot.sendMessage(
                                chatId = update.callbackQuery!!.message!!.chat.id,
                                text = "*$hashtag*\n$text",
                                parseMode = ParseMode.MARKDOWN,
                                disableWebPagePreview = true)
                    }
                }
            }
        }
    }

    fun startPolling() {
        bot.startPolling()
    }
}
