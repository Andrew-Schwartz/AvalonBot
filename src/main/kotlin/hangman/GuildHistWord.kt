package hangman

import common.util.loop
import io.ktor.util.*
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
        guildMessages[guild] = msg
        return@loop word.takeIf { it.all { it in 'a'..'z' || it in 'A'..'Z' } }
    }

    private suspend fun advanceMessage() {
        val msg = messages[idx]
        val num = Random.nextInt(5..10)
        val newMessages = runCatching { getMessages(GetChannelMessages.before(msg.channelId, msg, limit = num)) }.getOrNull()
        if (newMessages?.size == num) {
            val noGuild = newMessages.last()
            // we know what the guild is even though the Message obj in the response doesn't
            messages[idx] = with(noGuild) {
                Message(id, channelId, guild.id, author, member, content, timestamp,
                        editedTimestamp, tts, mentionsEveryone, mentions, emptyList(), attachments,
                        embeds, reactions, nonce, pinned, webhookId, type, activity, application)
            }
        } else {
            messages.removeAt(idx)
            if (messages.isEmpty()) throw GuildOutOfWordsException(guild)
        }
    }

    class GuildOutOfWordsException(guild: Guild) : RuntimeException("In ${guild.name}")

    companion object {
        private val map = mutableMapOf<Guild, GuildHistWord>()

        val guildMessages = mutableMapOf<Guild, Message>()
        fun noGuildData(guild: Guild): Boolean = guild !in map

        suspend fun forGuild(guild: Guild): GuildHistWord = map.getOrPut(guild) { GuildHistWord.new(guild) }

        suspend fun new(guild: Guild): GuildHistWord {
            val channels = guild.channels ?: getGuildChannels(guild)

            val messages = channels.map { it to it.lastMessageId }
                    .filter { (_, id) -> id != null }
                    .mapNotNull { (channel, id) ->
                        runCatching {
                            val num = Random.nextInt(90, 100)
                            val noGuild = getMessages(GetChannelMessages.before(channel, id!!, limit = num)).last()
                            // we know what the guild is even though the Message obj in the response doesn't
//                            noGuild.copy(guildId = guild.id) NPE's
                            with(noGuild) {
                                Message(this.id, channelId, guild.id, author, member, content, timestamp,
                                        editedTimestamp, tts, mentionsEveryone, mentions, emptyList(), attachments,
                                        embeds, reactions, nonce, pinned, webhookId, type, activity, application)
                            }
                        }.getOrNull()
                    }

            return GuildHistWord(guild, messages.toMutableList())
        }
    }
}