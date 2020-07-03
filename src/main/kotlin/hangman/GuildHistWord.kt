package hangman

import common.bot
import common.util.loop
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.rest.http.GetChannelMessages
import lib.rest.http.httpRequests.getGuildChannels
import lib.rest.http.httpRequests.getMessages
import kotlin.random.Random
import kotlin.random.nextInt

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class GuildHistWord private constructor(private val guild: Guild, private val messages: MutableList<Message>) : RandomWord {
    var idx = 0

    override suspend fun randomWord(): String = loop {
        val msg = messages[idx]
        advanceMessage()
        idx = (idx + 1) % messages.size
        if (msg.content.isEmpty()) return@loop null

        val word = msg.content.split("\\s+".toRegex()).random()
        return@loop word.takeIf { it.all { it in 'a'..'z' || it in 'A'..'Z' } }
    }

    private suspend fun advanceMessage() {
        val msg = messages[idx]
        val num = Random.nextInt(5..10)
        val newMessages = runCatching { bot.getMessages(GetChannelMessages.before(msg.channelId, msg, limit = num)) }.getOrNull()
        if (newMessages?.size == num) {
            messages[idx] = newMessages.last()
        } else {
            messages.removeAt(idx)
            if (messages.isEmpty()) throw GuildOutOfWordsException(guild)
        }
    }

    class GuildOutOfWordsException(guild: Guild) : RuntimeException("In ${guild.name}")

    companion object {
        private val map = mutableMapOf<Guild, GuildHistWord>()

        suspend fun forGuild(guild: Guild): GuildHistWord = map.getOrPut(guild) { GuildHistWord.new(guild) }

        suspend fun new(guild: Guild): GuildHistWord {
            val channels = guild.channels ?: bot.getGuildChannels(guild)

            val messages = channels.map { it to it.lastMessageId }
                    .filter { (_, id) -> id != null }
                    .mapNotNull { (channel, id) ->
                        runCatching {
                            bot.getMessages(GetChannelMessages.before(channel, id!!, limit = 100)).last()
                        }.getOrNull()
                    }

            return GuildHistWord(guild, messages.toMutableList())
        }
    }
}