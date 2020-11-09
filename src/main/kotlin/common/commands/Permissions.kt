package common.commands

// todo ???
inline class Permissions(val flags: Int) {
    companion object {
        // @formatter:off
        val CREATE_INSTANT_INVITE = Permissions(0x00000001)
        val KICK_MEMBERS          = Permissions(0x00000002)
        val BAN_MEMBERS           = Permissions(0x00000004)
        val ADMINISTRATOR         = Permissions(0x00000008)
        val MANAGE_CHANNELS       = Permissions(0x00000010)
        val MANAGE_GUILD          = Permissions(0x00000020)
        val ADD_REACTIONS         = Permissions(0x00000040)
        val VIEW_AUDIT_LOG        = Permissions(0x00000080)
        val PRIORITY_SPEAKER      = Permissions(0x00000100)
        val STREAM                = Permissions(0x00000200)
        val VIEW_CHANNEL          = Permissions(0x00000400)
        val SEND_MESSAGES         = Permissions(0x00000800)
        val SEND_TTS_MESSAGES     = Permissions(0x00001000)
        val MANAGE_MESSAGES       = Permissions(0x00002000)
        val EMBED_LINKS           = Permissions(0x00004000)
        val ATTACH_FILES          = Permissions(0x00008000)
        val READ_MESSAGE_HISTORY  = Permissions(0x00010000)
        val MENTION_EVERYONE      = Permissions(0x00020000)
        val USE_EXTERNAL_EMOJIS   = Permissions(0x00040000)
        val VIEW_GUILD_INSIGHTS   = Permissions(0x00080000)
        val CONNECT               = Permissions(0x00100000)
        val SPEAK                 = Permissions(0x00200000)
        val MUTE_MEMBERS          = Permissions(0x00400000)
        val DEAFEN_MEMBERS        = Permissions(0x00800000)
        val MOVE_MEMBERS          = Permissions(0x01000000)
        val USE_VAD               = Permissions(0x02000000)
        val CHANGE_NICKNAME       = Permissions(0x04000000)
        val MANAGE_NICKNAMES      = Permissions(0x08000000)
        val MANAGE_ROLES          = Permissions(0x10000000)
        val MANAGE_WEBHOOKS       = Permissions(0x20000000)
        val MANAGE_EMOJIS         = Permissions(0x40000000)
        // @formatter:on
    }
}