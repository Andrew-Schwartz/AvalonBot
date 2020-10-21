package lib.exceptions

abstract class Exceptions(message: String) : RuntimeException(message)

class EmbedLimitException(message: String) : Exceptions(message)

class InvalidSessionException(message: String = "") : Exceptions(message)

class PermissionException(message: String) : Exceptions(message)

class RequestException(message: String) : Exceptions(message)