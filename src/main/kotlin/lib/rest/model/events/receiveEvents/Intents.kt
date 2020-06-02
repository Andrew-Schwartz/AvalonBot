package lib.rest.model.events.receiveEvents

inline class Intents(val bits: Int = 0) {
    operator fun plus(other: Intents): Intents = Intents(bits or other.bits)

    companion object {
        val GUILDS = Intents(1 shl 0)
        val GUILD_MEMBERS = Intents(1 shl 1)
        val GUILD_BANS = Intents(1 shl 2)
        val GUILD_EMOJIS = Intents(1 shl 3)
        val GUILD_INTEGRATIONS = Intents(1 shl 4)
        val GUILD_WEBHOOKS = Intents(1 shl 5)
        val GUILD_INVITES = Intents(1 shl 6)
        val GUILD_VOICE_STATES = Intents(1 shl 7)
        val GUILD_PRESENCES = Intents(1 shl 8)
        val GUILD_MESSAGES = Intents(1 shl 9)
        val GUILD_MESSAGE_REACTIONS = Intents(1 shl 10)
        val GUILD_MESSAGE_TYPING = Intents(1 shl 11)
        val DIRECT_MESSAGES = Intents(1 shl 12)
        val DIRECT_MESSAGE_REACTIONS = Intents(1 shl 13)
        val DIRECT_MESSAGE_TYPING = Intents(1 shl 14)
    }
}