package lib.exceptions

abstract class DiscordException(message: String) : RuntimeException(message)

class EmbedLimitException(message: String) : DiscordException(message)

class MessageSendException(message: String) : DiscordException(message)

class InvalidSessionException(message: String = "") : DiscordException(message)

class PermissionException(message: String) : DiscordException(message)

class RequestException(message: String) : DiscordException(message)