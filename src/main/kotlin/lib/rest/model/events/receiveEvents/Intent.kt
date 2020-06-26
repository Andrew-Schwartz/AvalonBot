package lib.rest.model.events.receiveEvents

import common.util.A

inline class Intent(val bits: Int = 0) {
    operator fun plus(other: Intent): Intent = Intent(bits or other.bits)

    operator fun contains(other: Intent): Boolean {
        return other.bits and bits == other.bits
    }

    override fun toString(): String = "Intents(${(0..14).filter {
        val bit = 1 shl it
        bits and bit == bit
    }.joinToString(prefix = "[", postfix = "]") { intents[it] }}}"

    companion object Intents {
        private var value = Intent()

        // TODO this might work now that its not a big when
        private var sentValue = false

        override fun toString(): String = "$value, sent=$sentValue"

        operator fun plusAssign(other: Intent) {
            if (!sentValue) {
                value += other
            }
        }

        fun sendBits(): Int {
            sentValue = true
            return value.bits
        }

        val GUILDS = Intent(1 shl 0)
        val GUILD_MEMBERS = Intent(1 shl 1)
        val GUILD_BANS = Intent(1 shl 2)
        val GUILD_EMOJIS = Intent(1 shl 3)
        val GUILD_INTEGRATIONS = Intent(1 shl 4)
        val GUILD_WEBHOOKS = Intent(1 shl 5)
        val GUILD_INVITES = Intent(1 shl 6)
        val GUILD_VOICE_STATES = Intent(1 shl 7)
        val GUILD_PRESENCES = Intent(1 shl 8)
        val GUILD_MESSAGES = Intent(1 shl 9)
        val GUILD_MESSAGE_REACTIONS = Intent(1 shl 10)
        val GUILD_MESSAGE_TYPING = Intent(1 shl 11)
        val DIRECT_MESSAGES = Intent(1 shl 12)
        val DIRECT_MESSAGE_REACTIONS = Intent(1 shl 13)
        val DIRECT_MESSAGE_TYPING = Intent(1 shl 14)

        val intents = A[
                "GUILDS", "GUILD_MEMBERS", "GUILD_BANS", "GUILD_EMOJIS", "GUILD_INTEGRATIONS", "GUILD_WEBHOOKS",
                "GUILD_INVITES", "GUILD_VOICE_STATES", "GUILD_PRESENCES", "GUILD_MESSAGES", "GUILD_MESSAGE_REACTIONS",
                "GUILD_MESSAGE_TYPING", "DIRECT_MESSAGES", "DIRECT_MESSAGE_REACTIONS", "DIRECT_MESSAGE_TYPING"
        ]
    }
}